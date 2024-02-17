package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;

import com.tf4.photospot.auth.application.response.AuthUserInfoDto;
import com.tf4.photospot.auth.application.response.KakaoTokenInfoResponse;
import com.tf4.photospot.auth.infrastructure.KakaoClient;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoService {
	private final KakaoClient kakaoClient;

	public AuthUserInfoDto getTokenInfo(String accessToken, String id) {
		KakaoTokenInfoResponse response = kakaoClient.getTokenInfo(JwtConstant.PREFIX + accessToken);
		validateInfo(id, response);
		return new AuthUserInfoDto(String.valueOf(response.getId()));
	}

	private void validateInfo(String id, KakaoTokenInfoResponse response) {
		validateValue(String.valueOf(response.getId()), id);
		validateValue(String.valueOf(response.getAppId()), "app_id");
		validateExpiration(response.getExpiresIn());
	}

	private void validateValue(Object tokenValue, Object expectValue) {
		if (!expectValue.equals(tokenValue)) {
			throw new ApiException(AuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN);
		}
	}

	private void validateExpiration(Integer expiresIn) {
		if (expiresIn == null || expiresIn <= 0) {
			throw new ApiException(AuthErrorCode.EXPIRED_KAKAO_ACCESS_TOKEN);
		}
	}
}
