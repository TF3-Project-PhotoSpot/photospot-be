package com.tf4.photospot.global.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginUserDto {

	@Getter
	private Long id;
	private boolean hasLoggedInBefore;

	public LoginUserDto(Long id) {
		this.id = id;
		hasLoggedInBefore = true;
	}

	public LoginUserDto(Long id, boolean hasLoggedInBefore) {
		this.id = id;
		this.hasLoggedInBefore = hasLoggedInBefore;
	}

	public boolean hasLoggedInBefore() {
		return hasLoggedInBefore;
	}
}
