package com.tf4.photospot.user.application.response;

import com.tf4.photospot.user.domain.Role;
import com.tf4.photospot.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class OauthLoginUserResponse {

	private boolean hasLoggedInBefore;

	@Getter
	private Long id;

	@Getter
	private Role role;

	public boolean hasLoggedInBefore() {
		return hasLoggedInBefore;
	}

	public static OauthLoginUserResponse from(boolean hasLoggedInBefore, User user) {
		return new OauthLoginUserResponse(hasLoggedInBefore, user.getId(), user.getRole());
	}
}
