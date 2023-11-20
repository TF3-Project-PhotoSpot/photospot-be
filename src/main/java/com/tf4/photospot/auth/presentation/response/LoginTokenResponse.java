package com.tf4.photospot.auth.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoginTokenResponse {

	private boolean hasLoggedInBefore;
	private String accessToken;
	private String refreshToken;

}
