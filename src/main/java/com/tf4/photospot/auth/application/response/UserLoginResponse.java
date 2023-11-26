package com.tf4.photospot.auth.application.response;

import com.tf4.photospot.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {

	@Getter
	private boolean hasLoggedInBefore;

	@Getter
	private String account;

	public boolean hasLoggedInBefore() {
		return hasLoggedInBefore;
	}

	public static UserLoginResponse from(boolean hasLoggedInBefore, User user) {
		return new UserLoginResponse(hasLoggedInBefore, user.getAccount());
	}

}
