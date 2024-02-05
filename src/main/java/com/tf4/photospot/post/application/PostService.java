package com.tf4.photospot.post.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.global.exception.ApiErrorCode;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostPreviewListRequest;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostUploadResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.domain.MentionRepository;
import com.tf4.photospot.post.domain.Post;
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

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostService {

	private final PostQueryRepository postQueryRepository;
	private final PostJdbcRepository postJdbcRepository;
	private final PostRepository postRepository;
	private final PostTagRepository postTagRepository;
	private final SpotRepository spotRepository;
	private final UserRepository userRepository;
	private final MentionRepository mentionRepository;
	private final S3Uploader s3Uploader;

	public SlicePageDto<PostDetailResponse> getPosts(PostListRequest request) {
		final Slice<PostWithLikeStatus> postResponses = postQueryRepository.findPostsWithLikeStatus(request);
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

	public SlicePageDto<PostPreviewResponse> getPostPreviews(PostPreviewListRequest request) {
		return SlicePageDto.wrap(postQueryRepository.findPostPreviews(request));
	}

	@Transactional
	public PostUploadResponse upload(PostUploadRequest request) {
		User writer = userRepository.findById(request.getUserId())
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
		Spot spot = findSpotOrCreate(request.getSpotInfoDto());
		// Todo : bubble
		Photo photo = Photo.builder()
			.photoUrl(s3Uploader.copyToOtherDirectory(request.getPhotoUrl(), S3Directory.TEMP_FOLDER,
				S3Directory.POST_FOLDER))
			.coord(request.getPhotoCoord())
			.takenAt(request.getPhotoTakenAt())
			.build();
		Post post = Post.builder()
			.photo(photo)
			.spot(spot)
			.writer(writer)
			.detailAddress(request.getDetailAddress())
			.isPrivate(request.getIsPrivate())
			.build();
		return savePostAndRelatedEntities(post, spot.getId(), request.getTags(), request.getMentions());
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
		if (tagIds == null || tagIds.isEmpty()) {
			return;
		}
		int rowNum = postJdbcRepository.savePostTags(post.getId(), spotId, tagIds);
		validateRecordCount(rowNum, tagIds.size(), PostErrorCode.NOT_FOUND_TAG);
		post.addPostTags(postTagRepository.findByPostId(post.getId()));
	}

	public void saveMentions(Post post, List<Long> mentionedUserIds) {
		if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
			return;
		}
		int rowNum = postJdbcRepository.saveMentions(post.getId(), mentionedUserIds);
		validateRecordCount(rowNum, mentionedUserIds.size(), UserErrorCode.NOT_FOUND_USER);
		post.addMentions(mentionRepository.findByPostId(post.getId()));
	}

	private void validateRecordCount(int rowNum, int idNum, ApiErrorCode errorCode) {
		if (rowNum != idNum) {
			throw new ApiException(errorCode);
		}
	}
}
