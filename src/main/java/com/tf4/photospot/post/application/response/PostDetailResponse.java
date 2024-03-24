package com.tf4.photospot.post.application.response;

import java.time.LocalDateTime;
import java.util.List;

import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;

import lombok.Builder;

public record PostDetailResponse(
	Long id,
	String address,
	String detailAddress,
	int likeCount,
	String photoUrl,
	Boolean isLiked,
	LocalDateTime createdAt,
	BubbleResponse bubble,
	WriterResponse writer,
	List<TagResponse> tags
) {
	@Builder
	public PostDetailResponse {
	}

	public static PostDetailResponse of(PostDetail postDetail, List<PostTag> postTags, Long userId) {
		final Post post = postDetail.post();
		return PostDetailResponse.builder()
			.id(post.getId())
			.address(postDetail.spotAddress())
			.detailAddress(post.getDetailAddress())
			.likeCount(post.getLikeCount())
			.photoUrl(post.getPhoto().getPhotoUrl())
			.bubble(BubbleResponse.from(post.getPhoto().getBubble()))
			.createdAt(post.getCreatedAt())
			.writer(WriterResponse.from(post.getWriter(), userId))
			.isLiked(postDetail.isLiked())
			.tags(postTags.stream().map(TagResponse::from).toList())
			.build();
	}
}
