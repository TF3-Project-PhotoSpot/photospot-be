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

	public final String type;

	public static OauthAttributes findByType(String type) {
		return Arrays.stream(OauthAttributes.values())
			.filter(provider -> type.equals(provider.type))
			.findFirst()
			.orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_PROVIDER_TYPE));
	}
}
