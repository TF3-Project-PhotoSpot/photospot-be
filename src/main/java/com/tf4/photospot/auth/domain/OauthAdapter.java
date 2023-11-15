package com.tf4.photospot.auth.domain;

import java.util.HashMap;
import java.util.Map;

public class OauthAdapter {

	private OauthAdapter() {
	}

	public static Map<String, OauthRegistration> getOauthRegistrations(OauthProperties properties) {
		Map<String, OauthRegistration> oauthRegistrations = new HashMap<>();
		properties.getClient().forEach((key, value) -> oauthRegistrations.put(key,
			new OauthRegistration(value, properties.getProvider().get(key))));

		return oauthRegistrations;
	}
}
