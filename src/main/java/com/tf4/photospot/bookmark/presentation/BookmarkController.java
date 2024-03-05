package com.tf4.photospot.bookmark.presentation;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.application.response.BookmarkListResponse;
import com.tf4.photospot.bookmark.presentation.request.AddBookmarkHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.CreateBookmarkFolderHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.ReadBookmarkRequest;
import com.tf4.photospot.bookmark.presentation.response.AddBookmarkHttpResponse;
import com.tf4.photospot.bookmark.presentation.response.BookmarkFolderListHttpResponse;
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
			.build()
		);
		return new AddBookmarkHttpResponse(bookmarkId);
	}

	@GetMapping("/api/v1/bookmarkFolders/{bookmarkFolderId}/bookmarks")
	public BookmarkListResponse getBookmarks(
		@PathVariable(name = "bookmarkFolderId") Long bookmarkFolderId,
		@AuthUserId Long userId,
		@RequestParam(name = "postPreviewCount", defaultValue = "5")
		@Range(min = 1, max = 10, message = "미리보기 사진은 1~10개만 가능합니다.") Integer postPreviewCount,
		@PageableDefault Pageable pageable
	) {
		final PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
			Sort.by("id").descending());
		final ReadBookmarkRequest request = ReadBookmarkRequest.builder()
			.bookmarkFolderId(bookmarkFolderId)
			.userId(userId)
			.postPreviewCount(postPreviewCount)
			.pageable(pageRequest)
			.build();
		return bookmarkService.getBookmarks(request);
	}

	@GetMapping("/api/v1/bookmarkFolders")
	public BookmarkFolderListHttpResponse getBookmarkFolders(
		@AuthUserId Long userId
	) {
		return new BookmarkFolderListHttpResponse(bookmarkService.getBookmarkFolders(userId));
	}
}
