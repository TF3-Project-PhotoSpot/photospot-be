package com.tf4.photospot.auth.presentation;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	@GetMapping("/reissue")
	public ApiResponse<ReissueTokenResponse> reissueToken(
		@CookieValue(JwtConstant.REFRESH_COOKIE_NAME) String refreshToken) {
		return ApiResponse.success(authService.reissueToken(refreshToken));
	}
}
