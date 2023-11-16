package com.tf4.photospot.auth.util;

import java.util.HashMap;
import java.util.Map;

import com.tf4.photospot.auth.domain.OauthProperties;
import com.tf4.photospot.auth.domain.OauthRegistration;

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
