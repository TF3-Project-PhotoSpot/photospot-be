package com.tf4.photospot.auth.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.LoginUserDto;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	@GetMapping("/reissue")
	public ReissueTokenResponse reissueToken(
		@AuthenticationPrincipal LoginUserDto loginUserDto,
		@CookieValue(JwtConstant.REFRESH_COOKIE_NAME) String refreshToken) {
		return authService.reissueToken(loginUserDto.getId(), refreshToken);
	}
}
