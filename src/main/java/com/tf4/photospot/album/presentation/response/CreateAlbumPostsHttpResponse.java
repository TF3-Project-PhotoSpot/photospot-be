package com.tf4.photospot.album.presentation.response;

import java.util.List;

import com.tf4.photospot.album.application.response.CreateAlbumPostResponse;

public record CreateAlbumPostsHttpResponse(
	Boolean allAdded,
	List<CreateAlbumPostResponse> failedPosts
) {
	public static CreateAlbumPostsHttpResponse from(List<CreateAlbumPostResponse> responses) {
		final List<CreateAlbumPostResponse> createFailedAlbumPostResponses = responses.stream()
			.filter(CreateAlbumPostResponse::isDuplicated)
			.toList();
		return new CreateAlbumPostsHttpResponse(
			createFailedAlbumPostResponses.isEmpty(),
			createFailedAlbumPostResponses
		);
	}
}
