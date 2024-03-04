package com.tf4.photospot.bookmark.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.presentation.request.CreateBookmarkFolderHttpRequest;
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
}
