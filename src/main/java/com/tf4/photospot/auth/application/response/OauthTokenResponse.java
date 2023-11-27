package com.tf4.photospot.auth.application.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OauthTokenResponse(
	String accessToken,
	String scope,
	String tokenType) {
}
