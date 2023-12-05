package com.tf4.photospot.post.application.response;

public record PostThumbnailResponse(
	Long postId,
	String photoUrl
) {
}
