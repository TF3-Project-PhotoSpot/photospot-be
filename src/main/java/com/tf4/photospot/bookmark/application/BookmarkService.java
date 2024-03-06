package com.tf4.photospot.bookmark.application;

import org.springframework.stereotype.Service;

import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.bookmark.domain.BookmarkFolderRepository;
import com.tf4.photospot.bookmark.domain.BookmarkRepository;
import com.tf4.photospot.bookmark.infrastructure.BookmarkQueryRepository;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {
	private final UserService userService;
	private final BookmarkRepository bookmarkRepository;
	private final BookmarkQueryRepository bookmarkQueryRepository;
	private final BookmarkFolderRepository bookmarkFolderRepository;

	public Long createFolder(CreateBookmarkFolder createBookmarkFolder) {
		final User user = userService.getUser(createBookmarkFolder.userId());
		final BookmarkFolder bookmarkFolder = createBookmarkFolder.create(user);
		return bookmarkFolderRepository.save(bookmarkFolder).getId();
	}
}
