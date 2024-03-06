package com.tf4.photospot.bookmark.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BookmarkServiceTest extends IntegrationTestSupport {
	private final BookmarkService bookmarkService;
	private final UserRepository userRepository;

	@DisplayName("북마크 폴더 테스트")
	@TestFactory
	Stream<DynamicTest> bookmark() {
		final User user = userRepository.save(createUser("user"));
		//given
		final CreateBookmarkFolder createBookmarkFolder = CreateBookmarkFolder.builder()
			.userId(user.getId())
			.name("bookmarkFolder")
			.description("description")
			.color("color")
			.build();
		return Stream.of(
			dynamicTest("폴더를 생성한다", () ->
				assertDoesNotThrow(() -> bookmarkService.createFolder(createBookmarkFolder)))
		);
	}
}
