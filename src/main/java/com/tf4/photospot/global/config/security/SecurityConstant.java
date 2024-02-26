package com.tf4.photospot.global.config.security;

public interface SecurityConstant {
	String LOGIN_URL = "/api/v1/auth/login";
	String LOGOUT_URL = "/api/v1/auth/logout";
	String REISSUE_ACCESS_TOKEN_URL = "/api/v1/auth/reissue";
	String PROVIDER_TYPE_PARAM = "providerType";

	// kakao : 회원번호, apple : nonce 값
	String IDENTIFIER = "identifier";

	// kakao : 카카오 서버 access token, apple : 애플 서버 identify token
	String TOKEN = "token";
}
