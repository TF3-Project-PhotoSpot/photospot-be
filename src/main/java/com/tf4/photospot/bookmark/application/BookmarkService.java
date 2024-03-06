package com.tf4.photospot.bookmark.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.tf4.photospot.bookmark.infrastructure.BookmarkQueryRepository;
import com.tf4.photospot.bookmark.presentation.request.ReadBookmarkRequest;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.BookmarkErrorCode;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookmarkService {
	private final UserService userService;
	private final SpotService spotService;
	private final BookmarkRepository bookmarkRepository;
	private final BookmarkQueryRepository bookmarkQueryRepository;
	private final BookmarkFolderRepository bookmarkFolderRepository;

	@Transactional
	public Long createFolder(CreateBookmarkFolder createBookmarkFolder) {
		final User user = userService.getUser(createBookmarkFolder.userId());
		final BookmarkFolder bookmarkFolder = createBookmarkFolder.create(user);
		return bookmarkFolderRepository.save(bookmarkFolder).getId();
	}

	@Transactional
	public Long addBookmark(CreateBookmark createBookmark) {
		final BookmarkFolder bookmarkFolder = bookmarkFolderRepository.findById(createBookmark.bookmarkFolderId())
			.orElseThrow(() -> new ApiException(BookmarkErrorCode.INVALID_BOOKMARK_FOLDER_ID));
		final Spot spot = spotService.getSpot(createBookmark.spotId());
		final Bookmark bookmark = bookmarkFolder.add(createBookmark, spot);
		return bookmarkRepository.save(bookmark).getId();
	}

	public BookmarkListResponse getBookmarks(ReadBookmarkRequest request) {
		Slice<Bookmark> bookmarks = bookmarkQueryRepository.findBookmarksOfFolder(
			request.bookmarkFolderId(), request.userId(), request.pageable());
		final Map<Long, List<PostPreviewResponse>> postPreviewsGroupBySpot = spotService.getRecentPostPreviewsInSpots(
				bookmarks.map(Bookmark::getSpot).toList(), request.postPreviewCount())
			.stream()
			.collect(Collectors.groupingBy(PostPreviewResponse::spotId));
		final List<BookmarkResponse> bookmarkResponses = bookmarks.map(bookmark ->
			BookmarkResponse.of(bookmark, postPreviewsGroupBySpot.get(bookmark.getSpotId()))).getContent();
		return BookmarkListResponse.builder()
			.bookmarks(bookmarkResponses)
			.hasNext(bookmarks.hasNext())
			.build();
	}

	public List<BookmarkFolderResponse> getBookmarkFolders(ReadBookmarkFolderList request) {
		return bookmarkQueryRepository.findBookmarkFolders(request).stream()
			.map(BookmarkFolderResponse::from)
			.toList();
	}

	@Transactional
	public void removeBookmarks(Long bookmarkFolderId, Long userId, List<Long> bookmarkIds) {
		final BookmarkFolder bookmarkFolder = getMyBookmarkFolder(bookmarkFolderId, userId);
		final int removedBookmarkCount = bookmarkQueryRepository.deleteBookmarks(bookmarkFolder, bookmarkIds);
		bookmarkFolder.decrease(removedBookmarkCount);
	}

	@Transactional
	public void deleteBookmarkFolder(Long bookmarkFolderId, Long userId) {
		final BookmarkFolder bookmarkFolder = getMyBookmarkFolder(bookmarkFolderId, userId);
		bookmarkQueryRepository.deleteBookmarkFolder(bookmarkFolder);
	}

	private BookmarkFolder getMyBookmarkFolder(Long bookmarkFolderId, Long userId) {
		return bookmarkQueryRepository.findBookmarkFolder(bookmarkFolderId, userId)
			.orElseThrow(() -> new ApiException(BookmarkErrorCode.NO_AUTHORITY_BOOKMARK_FOLDER));
	}
}
