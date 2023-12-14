package com.tf4.photospot.post.application.response;

public record PostPreviewResponse(
	Long spotId,
	Long postId,
	String photoUrl
) {
}
