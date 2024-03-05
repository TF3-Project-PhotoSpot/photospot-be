package com.tf4.photospot.bookmark.presentation.response;

import java.util.List;

import com.tf4.photospot.bookmark.application.response.BookmarkFolderResponse;

public record BookmarkFolderListHttpResponse(
	List<BookmarkFolderResponse> bookmarkFolders
) {
}
