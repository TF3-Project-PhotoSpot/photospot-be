package com.tf4.photospot.global.config.jwt;

import java.time.Duration;

public interface JwtConstant {

	String PREFIX = "Bearer ";
	String AUTHORIZATION_HEADER = "Authorization";
	Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
	Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
	String USER_ID = "id";
	String USER_AUTHORITIES = "authorities";

}
