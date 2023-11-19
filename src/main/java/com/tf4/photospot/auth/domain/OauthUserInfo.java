package com.tf4.photospot.auth.domain;

import java.util.Arrays;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OauthUserInfo {

	private final String account;
	private final String name;

	// Todo : exception 처리
	public static OauthUserInfo of(String name, Map<String, Object> userAttributes) {
		return Arrays.stream(OauthAttributes.values())
			.filter(provider -> name.equals(provider.providerName))
			.findFirst()
			.orElseThrow(RuntimeException::new)
			.of(userAttributes);
	}
}
