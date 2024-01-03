package com.tf4.photospot.auth.domain;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthAttributes {

	KAKAO("kakao"),
	APPLE("apple");

	public final String type;

	public static Optional<OauthAttributes> findByType(String type) {
		return Arrays.stream(OauthAttributes.values())
			.filter(provider -> provider.type.equals(type))
			.findFirst();
	}
}
