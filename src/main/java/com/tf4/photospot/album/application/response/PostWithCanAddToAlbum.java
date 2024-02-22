package com.tf4.photospot.album.application.response;

import com.querydsl.core.annotations.QueryProjection;

public record PostWithCanAddToAlbum(
	Long postId,
	boolean isMyOrMentionedPost,
	boolean isExistsInAlbum
) {

	@QueryProjection
	public PostWithCanAddToAlbum {
	}

	public boolean canAddToAlbum() {
		return isMyOrMentionedPost && !isExistsInAlbum;
	}
}
