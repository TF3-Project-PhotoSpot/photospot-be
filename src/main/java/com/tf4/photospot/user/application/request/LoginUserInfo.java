package com.tf4.photospot.user.application.request;

import com.tf4.photospot.user.domain.User;

public record LoginUserInfo(String providerType, String account) {

	// 우선 name 이용하지 않고 랜덤 닉네임 생성
	public User toUser(String randomNickname) {
		return User.builder()
			.nickname(randomNickname)
			.providerType(providerType)
			.account(account)
			.build();
	}

}
