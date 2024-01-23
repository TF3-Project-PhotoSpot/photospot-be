package com.tf4.photospot.post.application.response;

import com.querydsl.core.annotations.QueryProjection;
import com.tf4.photospot.post.domain.Post;

public record PostWithLikedResponse(
	Post post,
	Boolean isLiked
) {
	@QueryProjection
	public PostWithLikedResponse {
	}
}
