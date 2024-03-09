package com.tf4.photospot.bookmark.application.response;

import java.util.List;

import lombok.Builder;

public record BookmarkListResponse(
	List<BookmarkResponse> bookmarks,
	Boolean hasNext
) {
	@Builder
	public BookmarkListResponse {
	}
}

