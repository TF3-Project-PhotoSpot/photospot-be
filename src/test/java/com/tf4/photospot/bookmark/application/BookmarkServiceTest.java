package com.tf4.photospot.bookmark.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.application.response.BookmarkListResponse;
import com.tf4.photospot.bookmark.application.response.BookmarkResponse;
import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.bookmark.domain.BookmarkFolderRepository;
import com.tf4.photospot.bookmark.domain.BookmarkRepository;
import com.tf4.photospot.bookmark.presentation.request.ReadBookmarkRequest;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BookmarkServiceTest extends IntegrationTestSupport {
	private final BookmarkService bookmarkService;
	private final UserRepository userRepository;
	private final SpotRepository spotRepository;
	private final BookmarkFolderRepository bookmarkFolderRepository;
	private final BookmarkRepository bookmarkRepository;
	private final PostRepository postRepository;

	@DisplayName("북마크 폴더 테스트")
	@TestFactory
	Stream<DynamicTest> bookmark() {
		final User user = userRepository.save(createUser("user"));
		final Spot spot = spotRepository.save(createSpot());
		final BookmarkFolder bookmarkFolder = bookmarkFolderRepository.save(createBookmarkFolder(user, "name"));
		//given
		final CreateBookmarkFolder createBookmarkFolder = CreateBookmarkFolder.builder()
			.userId(user.getId())
			.name("bookmarkFolder")
			.description("description")
			.color("color")
			.build();
		final CreateBookmark createBookmark = CreateBookmark.builder()
			.bookmarkFolderId(bookmarkFolder.getId())
			.userId(user.getId())
			.spotId(spot.getId())
			.name("bookmarkFolder")
			.description("description")
			.build();
		return Stream.of(
			dynamicTest("폴더를 생성한다", () ->
				assertDoesNotThrow(() -> bookmarkService.createFolder(createBookmarkFolder))),
			dynamicTest("폴더에 북마크를 추가한다", () ->
				assertDoesNotThrow(() -> bookmarkService.addBookmark(createBookmark)))
		);
	}

	@DisplayName("북마크 폴더의 북마크 미리보기를 조회한다.")
	@Test
	void getBookmarkOfFolderPreviews() {
		//given
		final User user = userRepository.save(createUser("이성빈"));
		final BookmarkFolder bookmarkFolder = bookmarkFolderRepository.save(createBookmarkFolder(user, "folder"));
		final Spot spot1 = spotRepository.save(createSpot());
		final Spot spot2 = spotRepository.save(createSpot());
		final Spot spot3 = spotRepository.save(createSpot());
		postRepository.saveAll(createList(() -> createPost(spot1, user, createPhoto()), 5));
		postRepository.saveAll(createList(() -> createPost(spot2, user, createPhoto()), 2));
		final Bookmark bookmark1 = bookmarkRepository.save(createBookmark(bookmarkFolder, spot1));
		final Bookmark bookmark2 = bookmarkRepository.save(createBookmark(bookmarkFolder, spot2));
		final Bookmark bookmark3 = bookmarkRepository.save(createBookmark(bookmarkFolder, spot3));
		final ReadBookmarkRequest request = ReadBookmarkRequest.builder()
			.bookmarkFolderId(bookmarkFolder.getId())
			.userId(user.getId())
			.postPreviewCount(5)
			.pageable(PageRequest.of(0, 10, Sort.by("id").descending()))
			.build();
		//when
		final BookmarkListResponse bookmarkPreviews = bookmarkService.getBookmarks(request);
		//then
		final List<BookmarkResponse> bookmarks = bookmarkPreviews.bookmarks();
		assertThat(bookmarkPreviews.hasNext()).isFalse();
		assertThat(bookmarks.get(0)).satisfies(
			response -> assertThat(response.id()).isEqualTo(bookmark3.getId()),
			response -> assertThat(response.spotId()).isEqualTo(spot3.getId()),
			response -> assertThat(response.photoUrls()).isEmpty()
		);
		assertThat(bookmarks.get(1)).satisfies(
			response -> assertThat(response.id()).isEqualTo(bookmark2.getId()),
			response -> assertThat(response.spotId()).isEqualTo(spot2.getId()),
			response -> assertThat(response.photoUrls().size()).isEqualTo(2)
		);
		assertThat(bookmarks.get(2)).satisfies(
			response -> assertThat(response.id()).isEqualTo(bookmark1.getId()),
			response -> assertThat(response.spotId()).isEqualTo(spot1.getId()),
			response -> assertThat(response.photoUrls().size()).isEqualTo(5)
		);
	}
}
