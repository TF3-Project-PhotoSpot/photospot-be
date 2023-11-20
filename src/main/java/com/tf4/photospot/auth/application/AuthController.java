package com.tf4.photospot.auth.application;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.presentation.AuthService;
import com.tf4.photospot.auth.presentation.response.LoginTokenResponse;
import com.tf4.photospot.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	@GetMapping("/login")
	public ApiResponse<LoginTokenResponse> login(@RequestParam String code, @RequestParam String provider) {
		return ApiResponse.success(authService.login(code, provider));
	}
}
