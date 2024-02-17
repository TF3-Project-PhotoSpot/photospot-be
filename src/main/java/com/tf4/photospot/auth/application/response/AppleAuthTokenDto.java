package com.tf4.photospot.auth.application.response;

import java.util.Date;

import lombok.Builder;

public record AppleAuthTokenDto(
	String subject,
	String nonce,
	String issuer,
	String audience,
	Date expiration
) {

	@Builder
	public AppleAuthTokenDto {
	}
}
