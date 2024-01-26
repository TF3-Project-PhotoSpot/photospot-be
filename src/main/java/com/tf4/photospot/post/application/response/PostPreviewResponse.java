package com.tf4.photospot.post.application.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

public record PostPreviewResponse(
	@JsonIgnore
	Long spotId,
	Long postId,
	String photoUrl
) {
	@QueryProjection
	public PostPreviewResponse {
	}
}
