package com.tf4.photospot.post.application.response;

import com.querydsl.core.annotations.QueryProjection;

public record ReportResponse(
	Long postId,
	Long writerId,
	String writerNickname,
	String spotAddress,
	String reason
) {
	@QueryProjection
	public ReportResponse {
	}
}
