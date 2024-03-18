package com.tf4.photospot.auth.application.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AppleRevokeRequest(
	String clientId,
	String clientSecret,
	String token
) {
	@Builder
	public AppleRevokeRequest {
	}
}
