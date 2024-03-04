package com.tf4.photospot.album.application.response;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;

public record AlbumPreviewResponse(
	Long albumId,
	String name,
	String photoUrl
) {
	private static final String NO_EXIST_PREVIEW = "";

	@Builder
	public AlbumPreviewResponse {
		if (StringUtils.isEmpty(photoUrl)) {
			photoUrl = NO_EXIST_PREVIEW;
		}
	}
}
