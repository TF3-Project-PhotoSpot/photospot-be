package com.tf4.photospot.bookmark.application.request;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateBookmarkFolderTest {
	@DisplayName("폴더 설명이 없으면 \"\"로 설정 된다.")
	@Test
	void createBookmarkFolder() {
		//given
		final CreateBookmarkFolder createBookmarkFolderWithoutDescription = CreateBookmarkFolder.builder()
			.name("name")
			.userId(1L)
			.color("color")
			.build();
		//when //then
		assertThat(createBookmarkFolderWithoutDescription.description()).isEqualTo("");
	}
}