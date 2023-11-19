package com.tf4.photospot.auth.domain;

import com.tf4.photospot.config.oauth.OauthProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
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
