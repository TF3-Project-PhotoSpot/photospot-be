package com.tf4.photospot.user.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.dto.LoginUserDto;
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

	@PostMapping("/profile")
	public UserProfileResponse updateProfile(@RequestPart("file") MultipartFile file,
		@RequestPart("request") UpdateProfileRequest request, @AuthenticationPrincipal LoginUserDto loginUser) {
		return userService.updateProfile(loginUser.getId(), file, request.type());
	}
}
