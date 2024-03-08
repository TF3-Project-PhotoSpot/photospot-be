package com.tf4.photospot.bookmark.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.Comparator;
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
import com.tf4.photospot.bookmark.application.request.ReadBookmarkFolderList;
import com.tf4.photospot.bookmark.application.response.BookmarkFolderResponse;
import com.tf4.photospot.bookmark.application.response.BookmarkListResponse;
import com.tf4.photospot.bookmark.application.response.BookmarkResponse;
import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.bookmark.domain.BookmarkFolderRepository;
import com.tf4.photospot.bookmark.domain.BookmarkRepository;
import com.tf4.photospot.bookmark.presentation.request.ReadBookmarkRequest;
import com.tf4.photospot.global.exception.domain.BookmarkErrorCode;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BookmarkServiceTest extends IntegrationTestSupport {
	private final BookmarkService bookmarkService;
	private final UserRepository userRepository;
	private final SpotRepository spotRepository;
	private final BookmarkFolderRepository bookmarkFolderRepository;
	private final BookmarkRepository bookmarkRepository;
	private final PostRepository postRepository;
	private final EntityManager em;

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
			.build();
		final ReadBookmarkFolderList readBookmarkFolderList = ReadBookmarkFolderList.builder()
			.userId(user.getId())
			.build();
		return Stream.of(
			dynamicTest("폴더를 생성한다", () ->
				assertDoesNotThrow(() -> bookmarkService.createFolder(createBookmarkFolder))),
			dynamicTest("폴더에 북마크를 추가한다", () ->
				assertDoesNotThrow(() -> bookmarkService.addBookmark(createBookmark))),
			dynamicTest("폴더에 북마크를 중복으로 추가할 수 없다", () ->
				assertThatThrownBy(() -> bookmarkService.addBookmark(createBookmark))
					.extracting("errorCode")
					.isEqualTo(BookmarkErrorCode.ALREADY_BOOKMARKED_IN_FOLDER)),
			dynamicTest("폴더 리스트를 조회한다", () -> {
				List<BookmarkFolderResponse> bookmarkFolderResponses = bookmarkService.getBookmarkFolders(
					readBookmarkFolderList);
				assertThat(bookmarkFolderResponses.size()).isEqualTo(2);
				assertThat(bookmarkFolderResponses).isSortedAccordingTo(
					Comparator.comparing(BookmarkFolderResponse::id).reversed());
				assertThat(bookmarkFolderResponses.get(1)).satisfies(
					response -> assertThat(response.id()).isEqualTo(bookmarkFolder.getId()),
					response -> assertThat(response.name()).isEqualTo(bookmarkFolder.getName()),
					response -> assertThat(response.description()).isEqualTo(bookmarkFolder.getDescription()),
					response -> assertThat(response.bookmarkCount()).isEqualTo(bookmarkFolder.getTotalCount()),
					response -> assertThat(response.color()).isEqualTo(bookmarkFolder.getColor())
				);
			}),
			dynamicTest("폴더에 북마크를 삭제한다", () -> {
				final BookmarkFolder folder = bookmarkFolderRepository.save(createBookmarkFolder(user, "folder"));
				final Long bookmarkId = bookmarkService.addBookmark(CreateBookmark.builder()
					.bookmarkFolderId(folder.getId())
					.userId(user.getId())
					.spotId(spot.getId())
					.name("bookmarkFolder")
					.build());
				final Bookmark bookmark = bookmarkRepository.findById(bookmarkId).get();
				bookmarkService.removeBookmarks(folder.getId(), user.getId(), List.of(bookmark.getId()));
				em.flush();
				em.clear();
				assertThat(bookmarkFolderRepository.findById(folder.getId())).isPresent().get()
					.satisfies(response -> assertThat(response.getTotalCount()).isZero());
				assertThat(bookmarkRepository.findById(bookmark.getId())).isEmpty();
			}),
			dynamicTest("삭제된 북마크 개수랑 요청 개수가 다르면 실패한다.", () -> {
				final BookmarkFolder folder = bookmarkFolderRepository.save(createBookmarkFolder(user, "folder"));
				final Long bookmarkId = bookmarkService.addBookmark(CreateBookmark.builder()
					.bookmarkFolderId(folder.getId())
					.userId(user.getId())
					.spotId(spot.getId())
					.name("bookmarkFolder")
					.build());
				final Bookmark bookmark = bookmarkRepository.findById(bookmarkId).get();
				assertThatThrownBy(() -> bookmarkService.removeBookmarks(folder.getId(), user.getId(),
					List.of(bookmark.getId(), 100L)))
					.extracting("errorCode")
					.isEqualTo(BookmarkErrorCode.DELETED_BOOKMARKS_DO_NOT_MATCH);
			}),
			dynamicTest("폴더 주인이 아니면 북마크를 삭제할 수 없다.", () -> {
				final Spot spot1 = spotRepository.save(createSpot());
				final Long bookmarkId = bookmarkService.addBookmark(CreateBookmark.builder()
					.bookmarkFolderId(bookmarkFolder.getId())
					.userId(user.getId())
					.spotId(spot1.getId())
					.name("bookmarkFolder")
					.build());
				final User otherUser = userRepository.save(createUser("user"));
				assertThatThrownBy(() -> bookmarkService.removeBookmarks(bookmarkFolder.getId(), otherUser.getId(),
					List.of(bookmarkId)))
					.extracting("errorCode")
					.isEqualTo(BookmarkErrorCode.NO_AUTHORITY_BOOKMARK_FOLDER);
			}),
			dynamicTest("내 폴더만 삭제할 수 있다.", () -> {
				final User otherUser = userRepository.save(createUser("user"));
				assertThatThrownBy(() ->
					bookmarkService.deleteBookmarkFolder(bookmarkFolder.getId(), otherUser.getId()))
					.extracting("errorCode")
					.isEqualTo(BookmarkErrorCode.NO_AUTHORITY_BOOKMARK_FOLDER);
			}),
			dynamicTest("폴더를 삭제한다.", () -> {
				assertDoesNotThrow(() -> bookmarkService.deleteBookmarkFolder(bookmarkFolder.getId(), user.getId()));
				em.clear();
				assertThat(bookmarkFolderRepository.findById(bookmarkFolder.getId())).isEmpty();
			})
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
