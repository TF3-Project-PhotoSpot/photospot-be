package com.tf4.photospot.user.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

	USER("user"),
	ADMIN("admin");

	public final String type;

	public static Role findByType(String type) {
		return Arrays.stream(Role.values())
			.filter(role -> type.equals(role.type))
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
	}
}
