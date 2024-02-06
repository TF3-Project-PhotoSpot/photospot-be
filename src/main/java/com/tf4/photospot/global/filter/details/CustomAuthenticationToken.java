package com.tf4.photospot.global.filter.details;

import java.util.Map;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

	private final Map<String, String> identityInfo;
	private final String providerType;

	public CustomAuthenticationToken(Map<String, String> identityInfo, String providerType) {
		super(null);
		this.identityInfo = identityInfo;
		this.providerType = providerType;
		setAuthenticated(false);
	}

	@Override
	public Object getPrincipal() {
		return identityInfo;
	}

	@Override
	public Object getCredentials() {
		return providerType;
	}
}
