package com.tf4.photospot.post.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostUploadRequest;

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
}
