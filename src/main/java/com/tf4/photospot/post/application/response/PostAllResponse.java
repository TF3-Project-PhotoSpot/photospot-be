package com.tf4.photospot.post.application.response;

import java.time.LocalDateTime;
import java.util.List;

import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;

import lombok.Builder;

public record PostAllResponse(
	Long id,
	String detailAddress,
	Long likeCount,
	String photoUrl,
	BubbleResponse bubble,
	Boolean isLiked,
	Boolean isPrivate,
	LocalDateTime createdAt,
	WriterResponse writer,
	List<TagResponse> tags,
	List<MentionResponse> mentions
) {
	@Builder
	public PostAllResponse {
	}

	public static PostAllResponse of(PostWithLikeStatus postResponse, List<PostTag> postTags, List<Mention> mentions) {
		final Post post = postResponse.post();
		return PostAllResponse.builder()
			.id(post.getId())
			.detailAddress(post.getDetailAddress())
			.likeCount(post.getLikeCount())
			.photoUrl(post.getPhoto().getPhotoUrl())
			.bubble(BubbleResponse.from(post.getPhoto().getBubble()))
			.createdAt(post.getCreatedAt())
			.writer(WriterResponse.from(post.getWriter()))
			.isLiked(postResponse.isLiked())
			.isPrivate(post.isPrivate())
			.tags(postTags.stream().map(TagResponse::from).toList())
			.mentions(mentions.stream().map(MentionResponse::from).toList())
			.build();
	}
}
