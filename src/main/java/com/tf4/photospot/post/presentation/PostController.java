package com.tf4.photospot.post.presentation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping()
	public ResponseEntity<?> uploadPost(
		@RequestPart("image") MultipartFile image
		// @RequestBody UploadPostRequest request
	) {
		postService.upload(new PostUploadRequest());

		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ApiResponse<SlicePageDto<PostDetailResponse>> getPostDetails(
		@RequestParam(name = "spotId") Long spotId,
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponse.success(postService.getPosts(new PostListRequest(spotId, userId, pageable)));
	}

	@GetMapping("/preview")
	public ApiResponse<SlicePageDto<PostDetailResponse>> getPostPreviews(
		@RequestParam(name = "spotId") Long spotId,
		@AuthUserId Long userId,
		@SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ApiResponse.success(postService.getPosts(new PostListRequest(spotId, userId, pageable)));
	}
}
