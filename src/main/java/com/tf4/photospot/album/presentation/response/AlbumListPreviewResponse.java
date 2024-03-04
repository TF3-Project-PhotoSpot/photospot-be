package com.tf4.photospot.album.presentation.response;

import java.util.List;

import com.tf4.photospot.album.application.response.AlbumPreviewResponse;

import lombok.Builder;

public record AlbumListPreviewResponse(
	String myPost,
	String likePost,
	List<AlbumPreviewResponse> albums
) {
	@Builder
	public AlbumListPreviewResponse {
	}
}
