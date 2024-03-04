package com.tf4.photospot.bookmark.application.request;

import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.user.domain.User;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;

public record CreateBookmarkFolder(
	String name,
	Long userId,
	String description,
	String color
) {
	private static final String EMPTY_DESCRIPTION = "";

	@Builder
	public CreateBookmarkFolder {
		if (StringUtils.isEmpty(description)) {
			description = EMPTY_DESCRIPTION;
		}
	}

	public BookmarkFolder create(User user) {
		return BookmarkFolder.builder()
			.user(user)
			.name(name)
			.description(description)
			.color(color)
			.build();
	}
}
