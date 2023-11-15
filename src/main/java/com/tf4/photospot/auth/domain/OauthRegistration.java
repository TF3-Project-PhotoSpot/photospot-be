package com.tf4.photospot.auth.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OauthRegistration {

	private final String clientId;
	private final String clientSecret;
	private final String redirectUri;
	private final String tokenUri;
	private final String userInfoUri;

	public OauthRegistration(OauthProperties.Client client, OauthProperties.Provider provider) {
		this(client.getClientId(), client.getClientSecret(), client.getRedirectUri(), provider.getTokenUri(),
			provider.getUserInfoUri());
	}
}
