package com.tf4.photospot.user.application.response;

import com.tf4.photospot.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class OauthLoginResponse {

	private boolean hasLoggedInBefore;

	@Getter
	private Long id;

	@Getter
	private String role;

	public boolean hasLoggedInBefore() {
		return hasLoggedInBefore;
	}

	public static OauthLoginResponse from(boolean hasLoggedInBefore, User user) {
		return new OauthLoginResponse(hasLoggedInBefore, user.getId(), user.getRole());
	}
}
