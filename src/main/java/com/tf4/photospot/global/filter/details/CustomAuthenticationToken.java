package com.tf4.photospot.global.filter.details;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

	private final String account;
	private final String providerType;

	public CustomAuthenticationToken(String account, String providerType) {
		super(null);
		this.account = account;
		this.providerType = providerType;
		setAuthenticated(false);
	}

	@Override
	public Object getPrincipal() {
		return account;
	}

	@Override
	public Object getCredentials() {
		return providerType;
	}

}
