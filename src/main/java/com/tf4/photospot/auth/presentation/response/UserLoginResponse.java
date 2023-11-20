package com.tf4.photospot.auth.presentation.response;

import com.tf4.photospot.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserLoginResponse {

	private boolean hasLoggedInBefore;
	private User user;

}
