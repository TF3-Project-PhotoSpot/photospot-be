package com.tf4.photospot.auth.presentation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.presentation.request.KakaoUnlinkCallbackInfo;
import com.tf4.photospot.auth.presentation.request.UnlinkRequest;
import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.ApiResponse;
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
		@RequestHeader(JwtConstant.AUTHORIZATION_HEADER) String refreshToken) {
		return authService.reissueToken(loginUserDto.getId(), refreshToken);
	}

	@PostMapping("/unlink")
	public ApiResponse unlinkUser(
		@AuthUserId Long userId,
		@RequestHeader(JwtConstant.AUTHORIZATION_HEADER) String accessToken,
		@RequestBody UnlinkRequest request
	) {
		authService.unlinkAccountFromOauthServer(userId, request.authorizationCode());
		authService.deleteUser(userId, accessToken);
		return ApiResponse.SUCCESS;
	}

	@GetMapping("/unlink/callback")
	public void deleteUnlinkedKakaoUser(
		@RequestHeader(JwtConstant.AUTHORIZATION_HEADER) String adminKey,
		@RequestParam("app_id") String appId,
		@RequestParam("user_id") String account,
		@RequestParam("referer_type") String refererType) {
		authService.deleteUnlinkedKakaoUser(adminKey, new KakaoUnlinkCallbackInfo(appId, account, refererType));
	}
}
