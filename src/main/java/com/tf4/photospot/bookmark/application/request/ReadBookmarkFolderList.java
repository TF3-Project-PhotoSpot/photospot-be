package com.tf4.photospot.bookmark.application.request;

import org.springframework.data.domain.Sort;

import lombok.Builder;

public record ReadBookmarkFolderList(
	Long userId,
	Sort.Direction direction
) {
	@Builder
	public ReadBookmarkFolderList {
		if (direction == null) {
			direction = Sort.Direction.DESC;
		}
	}

	public static ReadBookmarkFolderList of(Long userId, String direction) {
		return ReadBookmarkFolderList.builder()
			.userId(userId)
			.direction(Sort.Direction
				.fromOptionalString(direction)
				.orElseGet(() -> Sort.Direction.DESC))
			.build();
	}
}
