package com.tf4.photospot.global.config.security;

public interface SecurityConstant {
	String LOGIN_URL = "/api/v1/auth/login";
	String REISSUE_ACCESS_TOKEN_URL = "/api/v1/auth/reissue";
	String PROVIDER_TYPE_PARAM = "providerType";
	String ACCOUNT_PARAM = "account";
	String IDENTITY_TOKEN_PARAM = "identityToken";
	String NONCE_PARAM = "nonce";
}
