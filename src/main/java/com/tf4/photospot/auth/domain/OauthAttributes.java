package com.tf4.photospot.auth.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthAttributes {

	KAKAO("kakao"),
	APPLE("apple");

	public final String type;

	// Todo : 커스텀 예외 처리
	public static OauthAttributes findByType(String type) {
		return Arrays.stream(OauthAttributes.values())
			.filter(provider -> type.equals(provider.type))
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
	}
}
