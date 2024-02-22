package com.tf4.photospot.auth.application.response;

public record LoginResponse(
	String accessToken,
	String refreshToken,
	boolean hasLoggedInBefore) {
}
