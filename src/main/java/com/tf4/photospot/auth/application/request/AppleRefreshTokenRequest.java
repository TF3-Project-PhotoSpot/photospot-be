package com.tf4.photospot.auth.application.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AppleRefreshTokenRequest(
	String clientId,
	String clientSecret,
	String code,
	String grantType,
	String redirectUri
) {
	@Builder
	public AppleRefreshTokenRequest {
	}
}
