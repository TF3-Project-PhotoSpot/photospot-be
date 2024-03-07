package com.tf4.photospot.bookmark.presentation.response;

import java.util.List;

import com.tf4.photospot.bookmark.application.response.BookmarkFolderResponse;

import lombok.Builder;

public record BookmarkFolderListHttpResponse(
	List<BookmarkFolderResponse> bookmarkFolders,
	int maxBookmarkCount
) {
	@Builder
	public BookmarkFolderListHttpResponse {
	}
}
