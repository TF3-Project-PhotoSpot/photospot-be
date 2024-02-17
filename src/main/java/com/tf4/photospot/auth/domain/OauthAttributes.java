package com.tf4.photospot.auth.domain;

import java.util.Arrays;
import java.util.Map;

import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthAttributes {

	KAKAO("kakao"),
	APPLE("apple");

	public final String provider;

	public static OauthAttributes findByType(String provider) {
		return Arrays.stream(OauthAttributes.values())
			.filter(attributes -> attributes.provider.equals(provider))
			.findFirst()
			.orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_PROVIDER_TYPE));
	}

	public static String getAccount(Map<String, String> identityInfo) {
		return identityInfo.get(SecurityConstant.ACCOUNT_PARAM);
	}

	public static String getNonce(Map<String, String> identityInfo) {
		return identityInfo.get(SecurityConstant.NONCE_PARAM);
	}
}
