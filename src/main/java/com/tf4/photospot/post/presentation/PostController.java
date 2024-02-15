package com.tf4.photospot.post.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostPreviewListRequest;
import com.tf4.photospot.post.application.request.PostUpdateRequest;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostSaveResponse;
import com.tf4.photospot.post.application.response.PostUpdateResponse;
import com.tf4.photospot.post.presentation.request.PostStateUpdateRequest;
import com.tf4.photospot.post.presentation.request.PostUpdateHttpRequest;
import com.tf4.photospot.post.presentation.request.PostUploadHttpRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;

	@GetMapping
	public SlicePageDto<PostDetailResponse> getPostDetails(
		@RequestParam(name = "spotId") Long spotId,
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.spotId(spotId)
			.userId(userId)
			.type(PostSearchType.POSTS_OF_SPOT)
			.pageable(pageable)
			.build();
		return postService.getPosts(searchCondition);
	}

	@GetMapping("/preview")
	public SlicePageDto<PostPreviewResponse> getPostPreviews(
		@RequestParam(name = "spotId") Long spotId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.spotId(spotId)
			.type(PostSearchType.POSTS_OF_SPOT)
			.pageable(pageable)
			.build();
		return postService.getPostPreviews(searchCondition);
	}

	@PostMapping
	public PostSaveResponse uploadPost(@AuthUserId Long userId, @RequestBody @Valid PostUploadHttpRequest request) {
		return postService.upload(PostUploadRequest.of(userId, request));
	}

	@PostMapping("{postId}/likes")
	public ApiResponse likePost(
		@PathVariable(name = "postId") Long postId,
		@AuthUserId Long userId
	) {
		postService.likePost(postId, userId);
		return ApiResponse.SUCCESS;
	}

	@DeleteMapping("{postId}/likes")
	public ApiResponse cancelPostLike(
		@PathVariable(name = "postId") Long postId,
		@AuthUserId Long userId
	) {
		postService.likePost(postId, userId);
		return ApiResponse.SUCCESS;
	}

	@GetMapping("/mine/preview")
	public SlicePageDto<PostPreviewResponse> getMyPosts(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.MY_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPostPreviews(searchCondition);
	}

	@GetMapping("/mine")
	public SlicePageDto<PostDetailResponse> getMyPostDetails(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.MY_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPosts(searchCondition);
	}

	@GetMapping("/likes/preview")
	public SlicePageDto<PostPreviewResponse> getLikePostPreviews(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.LIKE_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPostPreviews(searchCondition);
	}

	@GetMapping("/likes")
	public SlicePageDto<PostDetailResponse> getLikePostDetails(
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		final PostSearchCondition searchCondition = PostSearchCondition.builder()
			.userId(userId)
			.type(PostSearchType.LIKE_POSTS)
			.pageable(pageable)
			.build();
		return postService.getPosts(searchCondition);
	}

	@PutMapping("/{id}")
	public PostUpdateResponse updatePost(@AuthUserId Long userId, @PathVariable("id") Long id,
		@RequestBody @Valid PostUpdateHttpRequest request) {
		return postService.update(PostUpdateRequest.of(userId, id, request));
	}

	@PatchMapping("/{id}")
	public PostUpdateResponse updateState(@AuthUserId Long userId, @PathVariable("id") Long id,
		@RequestBody @Valid PostStateUpdateRequest request) {
		return postService.updateState(userId, id, request.isPrivate());
	}

	@DeleteMapping("/{id}")
	public ApiResponse deletePost(@AuthUserId Long userId, @PathVariable("id") Long id) {
		postService.delete(userId, id);
		return ApiResponse.SUCCESS;
	}
}
