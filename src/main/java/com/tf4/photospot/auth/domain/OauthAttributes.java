package com.tf4.photospot.auth.domain;

import java.util.Arrays;

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
}
