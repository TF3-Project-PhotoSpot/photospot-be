package com.tf4.photospot.user.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.NicknameUpdateResponse;
import com.tf4.photospot.user.application.response.ProfileUpdateResponse;
import com.tf4.photospot.user.application.response.UserInfoResponse;
import com.tf4.photospot.user.presentation.request.NicknameUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

	private final UserService userService;
	private final S3Uploader s3Uploader;

	@PatchMapping("/profile/me")
	public ProfileUpdateResponse updateProfile(
		@RequestPart("file") MultipartFile file,
		@AuthUserId Long userId) {
		String imageUrl = s3Uploader.upload(file, S3Directory.PROFILE_FOLDER.getFolder());
		return userService.updateProfile(userId, imageUrl);
	}

	@PatchMapping("/nickname/me")
	public NicknameUpdateResponse updateNickname(
		@AuthUserId Long userId,
		@RequestBody NicknameUpdateRequest request) {
		return userService.updateNickname(userId, request.nickname());
	}

	@GetMapping("/me")
	public UserInfoResponse getUserInfo(@AuthUserId Long userId) {
		return userService.getInfo(userId);
	}
}
