package com.tf4.photospot.bookmark.presentation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.presentation.request.AddBookmarkHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.CreateBookmarkFolderHttpRequest;
import com.tf4.photospot.bookmark.presentation.response.AddBookmarkHttpResponse;
import com.tf4.photospot.bookmark.presentation.response.CreateBookmarkFolderResponse;
import com.tf4.photospot.global.argument.AuthUserId;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BookmarkController {
	private final BookmarkService bookmarkService;

	@PostMapping("/api/v1/bookmarkFolders")
	public CreateBookmarkFolderResponse createBookmarkFolder(
		@AuthUserId Long userId,
		@RequestBody @Valid CreateBookmarkFolderHttpRequest request
	) {
		final Long bookmarkFolderId = bookmarkService.createFolder(CreateBookmarkFolder.builder()
			.userId(userId)
			.name(request.name())
			.description(request.description())
			.color(request.color())
			.build());
		return new CreateBookmarkFolderResponse(bookmarkFolderId);
	}

	@PostMapping("/api/v1/bookmarkFolders/{bookmarkFolderId}/bookmarks")
	public AddBookmarkHttpResponse addBookmark(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@AuthUserId Long userId,
		@RequestBody @Valid AddBookmarkHttpRequest request
	) {
		final Long bookmarkId = bookmarkService.addBookmark(CreateBookmark.builder()
			.bookmarkFolderId(bookmarkFolderId)
			.userId(userId)
			.spotId(request.spotId())
			.name(request.name())
			.description(request.description())
			.build()
		);
		return new AddBookmarkHttpResponse(bookmarkId);
	}
}
