package com.tf4.photospot.bookmark.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.bookmark.domain.BookmarkFolderRepository;
import com.tf4.photospot.bookmark.domain.BookmarkRepository;
import com.tf4.photospot.bookmark.infrastructure.BookmarkQueryRepository;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.BookmarkErrorCode;
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
}
