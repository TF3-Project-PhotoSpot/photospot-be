package com.tf4.photospot.bookmark.presentation.response;

import java.util.List;

public record BookmarkCoordFolderListHttpResponse(
	List<BookmarkCoordFolderHttpResponse> bookmarkFolders
) {
}
