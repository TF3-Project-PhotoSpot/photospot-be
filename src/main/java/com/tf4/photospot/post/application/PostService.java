package com.tf4.photospot.post.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.aop.Retry;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostUpdateRequest;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostAllResponse;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostSaveResponse;
import com.tf4.photospot.post.application.response.PostUpdateResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.post.domain.MentionRepository;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostLikeRepository;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.PostTagRepository;
import com.tf4.photospot.post.infrastructure.PostJdbcRepository;
import com.tf4.photospot.post.infrastructure.PostQueryRepository;
import com.tf4.photospot.post.presentation.request.SpotInfoDto;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.utils.CollectionUtils;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostService {
	private final PostQueryRepository postQueryRepository;
	private final PostJdbcRepository postJdbcRepository;
	private final PostRepository postRepository;
	private final PostTagRepository postTagRepository;
	private final MentionRepository mentionRepository;
	private final SpotRepository spotRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;
	private final S3Uploader s3Uploader;

	public SlicePageDto<PostDetailResponse> getPosts(PostSearchCondition postSearchCond) {
		final Slice<PostWithLikeStatus> postResponses = postQueryRepository.findPostsWithLikeStatus(postSearchCond);
		final Map<Post, List<PostTag>> postTagGroup = postQueryRepository
			.findPostTagsIn(postResponses.stream().map(PostWithLikeStatus::post).toList())
			.stream()
			.collect(Collectors.groupingBy(PostTag::getPost));
		final List<PostDetailResponse> postDetailResponses = postResponses.stream()
			.map(postResponse -> PostDetailResponse.of(postResponse,
				postTagGroup.getOrDefault(postResponse.post(), Collections.emptyList())))
			.toList();
		return SlicePageDto.wrap(postDetailResponses, postResponses.hasNext());
	}

	public SlicePageDto<PostPreviewResponse> getPostPreviews(PostSearchCondition postSearchCond) {
		return SlicePageDto.wrap(postQueryRepository.findPostPreviews(postSearchCond));
	}

	public PostAllResponse getPost(Long userId, Long postId) {
		final PostWithLikeStatus postResponse = postQueryRepository.findPost(userId, postId);
		if (postResponse == null) {
			throw new ApiException(PostErrorCode.NOT_FOUND_POST);
		}
		final List<PostTag> postTags = postQueryRepository.findPostTagsIn(
			Collections.singletonList(postResponse.post()));
		final List<Mention> mentions = postQueryRepository.findMentionsIn(
			Collections.singletonList(postResponse.post()));
		return PostAllResponse.of(postResponse, postTags, mentions);
	}

	@Transactional
	public PostSaveResponse upload(PostUploadRequest request) {
		User writer = findLoginUser(request.userId());
		Spot spot = findSpotOrCreate(request.spotInfoDto());
		// Todo : bubble
		Photo photo = Photo.builder()
			.photoUrl(s3Uploader.copyToOtherDirectory(request.photoUrl(), S3Directory.TEMP_FOLDER,
				S3Directory.POST_FOLDER))
			.coord(request.photoCoord())
			.takenAt(request.photoTakenAt())
			.build();
		Post post = Post.builder()
			.photo(photo)
			.spot(spot)
			.writer(writer)
			.detailAddress(request.detailAddress())
			.isPrivate(request.isPrivate())
			.build();
		return savePostAndRelatedEntities(post, spot, request.tags(), request.mentions());
	}

	private PostSaveResponse savePostAndRelatedEntities(Post post, Spot spot, List<Long> tagIds,
		List<Long> mentionIds) {
		try {
			postRepository.save(post);
			spot.incPostCount();
			savePostTags(post.getId(), spot.getId(), tagIds);
			saveMentions(post.getId(), mentionIds);
			return new PostSaveResponse(post.getId(), post.getSpot().getId());
		} catch (Exception ex) {
			s3Uploader.deleteFile(post.getPhoto().getPhotoUrl(), S3Directory.POST_FOLDER);
			throw ex;
		}
	}

	private Spot findSpotOrCreate(SpotInfoDto spotInfoDto) {
		return spotRepository.findByCoord(spotInfoDto.coord().toCoord())
			.orElseGet(() -> spotRepository.save(spotInfoDto.toSpot()));
	}

	public void savePostTags(Long postId, Long spotId, List<Long> tagIds) {
		if (CollectionUtils.isNullOrEmpty(tagIds)) {
			return;
		}
		if (!postJdbcRepository.savePostTags(postId, spotId, tagIds)) {
			throw new ApiException(PostErrorCode.NOT_FOUND_TAG);
		}
	}

	public void saveMentions(Long postId, List<Long> mentionedUserIds) {
		if (CollectionUtils.isNullOrEmpty(mentionedUserIds)) {
			return;
		}
		if (!postJdbcRepository.saveMentions(postId, mentionedUserIds)) {
			throw new ApiException(UserErrorCode.NOT_FOUND_USER);
		}
	}

	@Retry
	@Transactional
	public void likePost(Long postId, Long userId) {
		final User user = findLoginUser(userId);
		final Post post = findPost(postId);
		if (postQueryRepository.existsPostLike(post, user)) {
			throw new ApiException(PostErrorCode.ALREADY_LIKE);
		}
		final PostLike postLike = post.likeFrom(user);
		postLikeRepository.save(postLike);
	}

	@Retry
	@Transactional
	public void cancelPostLike(Long postId, Long userId) {
		final PostLike postLike = postQueryRepository.findPostLikeFetch(postId, userId)
			.orElseThrow(() -> new ApiException(PostErrorCode.NO_EXISTS_LIKE));
		postLike.cancel();
		postLikeRepository.delete(postLike);
	}

	@Transactional
	public PostUpdateResponse update(PostUpdateRequest request) {
		User loginUser = findLoginUser(request.userId());
		Post post = findPost(request.postId());
		post.updateDetailAddress(loginUser, request.detailAddress());
		updatePostTags(post, request.tags());
		updateMentions(post, request.mentions());
		return new PostUpdateResponse(post.getId(), post.getSpot().getId());
	}

	private void updatePostTags(Post post, List<Long> tagIds) {
		postJdbcRepository.deletePostTagsByPostId(post.getId());
		savePostTags(post.getId(), post.getSpot().getId(), tagIds);
	}

	private void updateMentions(Post post, List<Long> mentionedUserIds) {
		postJdbcRepository.deleteMentionsByPostId(post.getId());
		saveMentions(post.getId(), mentionedUserIds);
	}

	@Transactional
	public PostUpdateResponse updatePrivacyState(Long userId, Long postId, boolean isPrivate) {
		User loginUser = findLoginUser(userId);
		Post post = findPost(postId);
		post.updatePrivacyState(loginUser, isPrivate);
		return new PostUpdateResponse(post.getId(), post.getSpot().getId());
	}

	// Todo : 방명록 존재하지 않는 스팟 바로바로 확인해서 삭제 해야 할지? 아니면 스케줄러?
	@Transactional
	public void delete(Long userId, Long postId) {
		User loginUser = findLoginUser(userId);
		Post post = findPost(postId);
		post.delete(loginUser);
		postTagRepository.deleteByPostId(postId);
		mentionRepository.deleteByPostId(postId);
	}

	private User findLoginUser(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
	}

	private Post findPost(Long postId) {
		return postRepository.findById(postId).orElseThrow(() -> new ApiException(PostErrorCode.NOT_FOUND_POST));
	}

	public Optional<String> getFirstPostImage(PostSearchCondition postSearchCond) {
		List<PostPreviewResponse> postPreviews = postQueryRepository.findPostPreviews(postSearchCond).getContent();
		if (postPreviews.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(postPreviews.get(0)).map(PostPreviewResponse::photoUrl);
	}
}
