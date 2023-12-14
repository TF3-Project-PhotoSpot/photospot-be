package com.tf4.photospot.auth.application.response;

public record LoginTokenResponse(
	boolean hasLoggedInBefore,
	String accessToken,
	String refreshToken) {
}
