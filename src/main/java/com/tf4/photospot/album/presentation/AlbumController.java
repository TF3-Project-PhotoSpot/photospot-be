package com.tf4.photospot.album.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.album.application.AlbumService;
import com.tf4.photospot.album.presentation.request.CreateAlbumHttpRequest;
import com.tf4.photospot.album.presentation.request.PostIdListHttpRequest;
import com.tf4.photospot.album.presentation.response.CreateAlbumHttpResponse;
import com.tf4.photospot.album.presentation.response.CreateAlbumPostsHttpResponse;
import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/albums")
@RestController
@RequiredArgsConstructor
public class AlbumController {
	private final AlbumService albumService;

	@GetMapping("/{albumId}/posts/preview")
	public SlicePageDto<PostPreviewResponse> getAlbumPostPreviews(
		@PathVariable(name = "albumId") Long albumId,
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.albumId(albumId)
			.type(PostSearchType.ALBUM_POSTS)
			.pageable(pageable)
			.build();
		return albumService.getPostPreviewsOfAlbum(searchCondition);
	}

	@GetMapping("/{albumId}/posts")
	public SlicePageDto<PostDetailResponse> getAlbumPostDetails(
		@PathVariable(name = "albumId") Long albumId,
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.albumId(albumId)
			.type(PostSearchType.ALBUM_POSTS)
			.pageable(pageable)
			.build();
		return albumService.getPostsOfAlbum(searchCondition);
	}

	@PostMapping
	public CreateAlbumHttpResponse createAlbum(
		@RequestBody CreateAlbumHttpRequest request,
		@AuthUserId Long userId
	) {
		return new CreateAlbumHttpResponse(albumService.create(userId, request.name()));
	}

	@PostMapping("/{albumId}/posts")
	public CreateAlbumPostsHttpResponse addPosts(
		@PathVariable(name = "albumId") Long albumId,
		@AuthUserId Long userId,
		@RequestBody @Valid PostIdListHttpRequest request
	) {
		return CreateAlbumPostsHttpResponse.from(albumService.addPosts(request.postIds(), albumId, userId));
	}

	@PatchMapping("/{albumId}/posts")
	public ApiResponse replacePosts(
		@PathVariable(name = "albumId") Long albumId,
		@AuthUserId Long userId,
		@RequestBody @Valid PostIdListHttpRequest request
	) {
		albumService.replacePosts(request.postIds(), albumId, userId);
		return ApiResponse.SUCCESS;
	}

	@DeleteMapping("/{albumId}/posts")
	public ApiResponse removePosts(
		@PathVariable(name = "albumId") Long albumId,
		@AuthUserId Long userId,
		@RequestBody @Valid PostIdListHttpRequest request
	) {
		albumService.removePosts(request.postIds(), albumId, userId);
		return ApiResponse.SUCCESS;
	}

	@DeleteMapping("/{albumId}")
	public ApiResponse removeAlbum(
		@PathVariable(name = "albumId") Long albumId,
		@AuthUserId Long userId
	) {
		albumService.remove(albumId, userId);
		return ApiResponse.SUCCESS;
	}
}
