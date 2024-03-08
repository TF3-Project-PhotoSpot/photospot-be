package com.tf4.photospot.bookmark.presentation.response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tf4.photospot.bookmark.application.response.BookmarkCoord;

import lombok.Builder;

public record BookmarkCoordFolderHttpResponse(
	Long bookmarkFolderId,
	String color,
	List<BookmarkCoordHttpResponse> bookmarks
) {
	@Builder
	public BookmarkCoordFolderHttpResponse {
	}

	public static List<BookmarkCoordFolderHttpResponse> from(List<BookmarkCoord> bookmarkCoords) {
		final Map<Long, List<BookmarkCoord>> bookmarkCoordGroupByFolder = bookmarkCoords.stream()
			.collect(Collectors.groupingBy(BookmarkCoord::bookmarkFolderId));
		return bookmarkCoordGroupByFolder.values().stream()
			.map(bookmarkGroup -> {
				BookmarkCoord bookmarkCoord = bookmarkGroup.get(0);
				return BookmarkCoordFolderHttpResponse.builder()
					.bookmarkFolderId(bookmarkCoord.bookmarkFolderId())
					.color(bookmarkCoord.bookmarkFolderColor())
					.bookmarks(bookmarkGroup.stream().map(BookmarkCoordHttpResponse::from).toList())
					.build();
			})
			.toList();
	}
}
