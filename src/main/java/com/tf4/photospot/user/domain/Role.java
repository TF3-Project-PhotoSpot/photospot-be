package com.tf4.photospot.user.domain;

import java.util.Arrays;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

	USER("ROLE_USER"),
	ADMIN("ROLE_ADMIN,ROLE_USER");

	public final String type;

	public static Role findByType(String type) {
		return Arrays.stream(Role.values())
			.filter(role -> Arrays.asList(role.getType().split(",")).contains(type))
			.findFirst()
			.orElseThrow(() -> new ApiException(AuthErrorCode.INVALID_ROLE));
	}
}
