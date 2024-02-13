package com.tf4.photospot.post.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostUploadResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostLikeRepository;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.post.domain.PostTag;
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

	@Transactional
	public PostUploadResponse upload(PostUploadRequest request) {
		User writer = userRepository.findById(request.userId())
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
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
		return savePostAndRelatedEntities(post, spot.getId(), request.tags(), request.mentions());
	}

	private PostUploadResponse savePostAndRelatedEntities(Post post, Long spotId, List<Long> tagIds,
		List<Long> mentionIds) {
		try {
			postRepository.save(post);
			savePostTags(post, spotId, tagIds);
			saveMentions(post, mentionIds);
			return new PostUploadResponse(post.getId());
		} catch (Exception ex) {
			s3Uploader.deleteFile(post.getPhoto().getPhotoUrl(), S3Directory.POST_FOLDER);
			throw ex;
		}
	}

	private Spot findSpotOrCreate(SpotInfoDto spotInfoDto) {
		return spotRepository.findByCoord(spotInfoDto.coord().toCoord())
			.orElseGet(() -> spotRepository.save(spotInfoDto.toSpot()));
	}

	public void savePostTags(Post post, Long spotId, List<Long> tagIds) {
		if (CollectionUtils.isNullOrEmpty(tagIds)) {
			return;
		}
		if (!postJdbcRepository.savePostTags(post.getId(), spotId, tagIds)) {
			throw new ApiException(PostErrorCode.NOT_FOUND_TAG);
		}
	}

	public void saveMentions(Post post, List<Long> mentionedUserIds) {
		if (CollectionUtils.isNullOrEmpty(mentionedUserIds)) {
			return;
		}
		if (!postJdbcRepository.saveMentions(post.getId(), mentionedUserIds)) {
			throw new ApiException(UserErrorCode.NOT_FOUND_USER);
		}
	}

	@Retry
	@Transactional
	public void likePost(Long postId, Long userId) {
		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		final Post post = postRepository.findById(postId)
			.orElseThrow(() -> new ApiException(PostErrorCode.NOT_FOUND_POST));
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
}
