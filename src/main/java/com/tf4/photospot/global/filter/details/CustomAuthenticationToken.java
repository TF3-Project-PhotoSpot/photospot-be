package com.tf4.photospot.global.filter.details;

import java.util.Map;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

	private final Map<String, String> identifyInfo;
	private final String providerType;

	public CustomAuthenticationToken(Map<String, String> identifyInfo, String providerType) {
		super(null);
		this.identifyInfo = identifyInfo;
		this.providerType = providerType;
		setAuthenticated(false);
	}

	@Override
	public Object getPrincipal() {
		return identifyInfo;
	}

	@Override
	public Object getCredentials() {
		return providerType;
	}
}
