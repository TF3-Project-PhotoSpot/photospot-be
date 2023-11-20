package com.tf4.photospot.auth.domain.oauth;

import java.util.Arrays;
import java.util.Map;

import com.tf4.photospot.user.domain.User;

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

	// Todo : 닉네임 랜덤 생성 추가
	public User toUser(String provider) {
		return new User(null, null, name, null, provider, account, null);
	}
}
