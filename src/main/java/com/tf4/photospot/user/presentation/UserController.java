package com.tf4.photospot.user.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.UserProfileResponse;
import com.tf4.photospot.user.presentation.request.UpdateProfileRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

	private final UserService userService;
	private final S3Uploader s3Uploader;

	@PostMapping("/profile")
	public UserProfileResponse updateProfile(@RequestPart("file") MultipartFile file,
		@RequestPart("request") UpdateProfileRequest request, @AuthUserId Long userId) {
		String imageUrl = s3Uploader.upload(file, request.type());
		return userService.updateProfile(userId, imageUrl);
	}
}
