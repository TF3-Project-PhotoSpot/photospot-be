package com.tf4.photospot.bookmark.application.request;

import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.spot.domain.Spot;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;

public record CreateBookmark(
	Long bookmarkFolderId,
	Long userId,
	Long spotId,
	String name
) {
	private static final String EMPTY_NAME = "";

	@Builder
	public CreateBookmark {
		if (StringUtils.isEmpty(EMPTY_NAME)) {
			name = EMPTY_NAME;
		}
	}

	public Bookmark create(BookmarkFolder bookmarkFolder, Spot spot) {
		return Bookmark.builder()
			.spot(spot)
			.bookmarkFolder(bookmarkFolder)
			.name(name)
			.build();
	}
}
