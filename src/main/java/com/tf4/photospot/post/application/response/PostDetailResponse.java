package com.tf4.photospot.post.application.response;

import java.time.LocalDateTime;
import java.util.List;

import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.user.domain.User;

import lombok.Builder;

public record PostDetailResponse(
	// Post: id, detail address, tag, createdAt, likeCount, photoUrl, bubble
	Long id,
	String detailAddress,
	Long likeCount,
	String photoUrl,
	Boolean isLiked,
	LocalDateTime createdAt,
	PostUserResponse writer,
	List<TagResponse> tags
) {
	@Builder
	public PostDetailResponse {
	}

	public static PostDetailResponse of(PostWithLikedResponse postResponse, List<PostTag> postTags) {
		final Post post = postResponse.post();
		return PostDetailResponse.builder()
			.id(post.getId())
			.detailAddress(post.getDetailAddress())
			.likeCount(post.getLikeCount())
			.photoUrl(post.getPhoto().getPhotoUrl())
			.createdAt(post.getCreatedAt())
			.writer(PostUserResponse.from(post.getWriter()))
			.isLiked(postResponse.isLiked())
			.tags(postTags.stream().map(TagResponse::from).toList())
			.build();
	}

	public record PostUserResponse(
		Long id,
		String nickname,
		String profileUrl
	) {
		@Builder
		public PostUserResponse {
		}

		public static PostUserResponse from(User user) {
			return PostUserResponse.builder()
				.id(user.getId())
				.nickname(user.getNickname())
				.profileUrl(user.getProfileUrl())
				.build();
		}
	}

	public record TagResponse(
		Long tagId,
		String iconUrl,
		String tagName
	) {
		@Builder
		public TagResponse {
		}

		public static TagResponse from(PostTag postTag) {
			final Tag tag = postTag.getTag();
			return TagResponse.builder()
				.tagId(tag.getId())
				.iconUrl(tag.getIconUrl())
				.tagName(tag.getName())
				.build();
		}
	}
}
