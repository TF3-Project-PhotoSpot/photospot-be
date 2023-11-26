package com.tf4.photospot.auth.domain.oauth;

import java.util.Map;

import com.tf4.photospot.user.domain.User;


public record OauthUserInfo (String account) {

	public static OauthUserInfo of(String type, Map<String, Object> userAttributes) {
		return OauthAttributes.findByProviderType(type).of(userAttributes);
	}

	// 우선 name 이용하지 않고 랜덤 닉네임 생성
	public User toUser(String provider, String randomNickname) {
		return new User(randomNickname, provider, account);
	}

}
