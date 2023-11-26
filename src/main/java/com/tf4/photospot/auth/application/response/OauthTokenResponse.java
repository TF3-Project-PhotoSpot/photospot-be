package com.tf4.photospot.auth.application.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OauthTokenResponse(
	@JsonProperty("access_token") String accessToken,
	String scope,
	@JsonProperty("token_type") String tokenType) {
}
