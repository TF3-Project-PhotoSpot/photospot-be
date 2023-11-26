package com.tf4.photospot.auth.util.oauth;

import java.util.HashMap;
import java.util.Map;

import com.tf4.photospot.auth.domain.oauth.OauthRegistration;
import com.tf4.photospot.config.oauth.OauthProperties;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OauthAdapter {

	public static Map<String, OauthRegistration> createOauthRegistrations(OauthProperties properties) {
		Map<String, OauthRegistration> oauthRegistrations = new HashMap<>();
		properties.getClient().forEach((key, value) -> oauthRegistrations.put(key,
			new OauthRegistration(value, properties.getProvider().get(key))));

		return oauthRegistrations;
	}
}
