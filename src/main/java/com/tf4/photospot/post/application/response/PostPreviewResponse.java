package com.tf4.photospot.post.application.response;

import com.querydsl.core.annotations.QueryProjection;

public record PostPreviewResponse(
	Long spotId,
	Long postId,
	String photoUrl
) {
	@QueryProjection
	public PostPreviewResponse {
	}
}
