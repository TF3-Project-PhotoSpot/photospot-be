package com.tf4.photospot.auth.domain.oauth;

import java.util.Arrays;
import java.util.Map;

import com.tf4.photospot.auth.util.NicknameGenerator;
import com.tf4.photospot.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OauthUserInfo {

	private final String account;

	// Todo : exception 처리
	public static OauthUserInfo of(String name, Map<String, Object> userAttributes) {
		return Arrays.stream(OauthAttributes.values())
			.filter(provider -> name.equals(provider.providerName))
			.findFirst()
			.orElseThrow(RuntimeException::new)
			.of(userAttributes);
	}

	// 우선 name 이용하지 않고 랜덤 닉네임 생성
	public User toUser(String provider) {
		return new User(null, null, NicknameGenerator.generatorRandomNickname(),
			null, provider, account, null);
	}
}
