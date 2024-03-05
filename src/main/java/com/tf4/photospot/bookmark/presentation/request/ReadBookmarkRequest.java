package com.tf4.photospot.bookmark.presentation.request;

import org.springframework.data.domain.Pageable;

import lombok.Builder;

public record ReadBookmarkRequest(
	Long bookmarkFolderId,
	Long userId,
	int postPreviewCount,
	Pageable pageable
) {
	@Builder
	public ReadBookmarkRequest {
	}
}
