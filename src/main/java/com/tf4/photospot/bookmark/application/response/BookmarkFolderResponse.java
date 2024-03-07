package com.tf4.photospot.bookmark.application.response;

import com.tf4.photospot.bookmark.domain.BookmarkFolder;

import lombok.Builder;

public record BookmarkFolderResponse(
	Long id,
	String name,
	String description,
	String color,
	int bookmarkCount
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
			.build();
	}
}
