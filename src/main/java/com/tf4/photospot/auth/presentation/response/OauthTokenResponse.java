package com.tf4.photospot.auth.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OauthTokenResponse {

	private String accessToken;
	private String scope;
	private String tokenType;

}
