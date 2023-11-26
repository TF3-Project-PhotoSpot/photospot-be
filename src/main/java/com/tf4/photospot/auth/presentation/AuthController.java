package com.tf4.photospot.auth.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.domain.oauth.OauthAttributes;
import com.tf4.photospot.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	// Todo : name 설정 생략 시 IllegalArgumentException 발생 => 확인 후 수정
	@GetMapping("/login/kakao")
	public ApiResponse<LoginTokenResponse> loginByKakao(@RequestParam("code") String code) {
		return ApiResponse.success(authService.login(code, OauthAttributes.KAKAO.type));
	}
}
