package com.tf4.photospot.post.application.response;

import java.time.LocalDateTime;
import java.util.List;

import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;

import lombok.Builder;

public record PostDetailResponse(
	Long id,
	String detailAddress,
	int likeCount,
	String photoUrl,
	BubbleResponse bubble,
	Boolean isLiked,
	LocalDateTime createdAt,
	WriterResponse writer,
	List<TagResponse> tags
) {
	@Builder
	public PostDetailResponse {
	}

	public static PostDetailResponse of(PostWithLikeStatus postResponse, List<PostTag> postTags) {
		final Post post = postResponse.post();
		return PostDetailResponse.builder()
			.id(post.getId())
			.detailAddress(post.getDetailAddress())
			.likeCount(post.getLikeCount())
			.photoUrl(post.getPhoto().getPhotoUrl())
			.bubble(BubbleResponse.from(post.getPhoto().getBubble()))
			.createdAt(post.getCreatedAt())
			.writer(WriterResponse.from(post.getWriter()))
			.isLiked(postResponse.isLiked())
			.tags(postTags.stream().map(TagResponse::from).toList())
			.build();
	}
}
