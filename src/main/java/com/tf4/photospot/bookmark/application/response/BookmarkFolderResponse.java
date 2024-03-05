package com.tf4.photospot.bookmark.application.response;

import com.tf4.photospot.bookmark.domain.BookmarkFolder;

import lombok.Builder;

public record BookmarkFolderResponse(
	Long id,
	String name,
	String description,
	String color,
	int bookmarkCount,
	int maxBookmarkCount
) {
	@Builder
	public BookmarkFolderResponse {
	}

	public static BookmarkFolderResponse from(BookmarkFolder bookmarkFolder) {
		return BookmarkFolderResponse.builder()
			.id(bookmarkFolder.getId())
			.name(bookmarkFolder.getName())
			.description(bookmarkFolder.getDescription())
			.color(bookmarkFolder.getColor())
			.bookmarkCount(bookmarkFolder.getTotalCount())
			.maxBookmarkCount(BookmarkFolder.MAX_BOOKMARKED)
			.build();
	}
}
