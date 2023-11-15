package com.tf4.photospot.auth.infrastructure;

import java.util.Map;

import com.tf4.photospot.auth.domain.OauthRegistration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryRegistrationsRepository {

	private final Map<String, OauthRegistration> registrations;

	public OauthRegistration findByProviderName(String name) {
		return registrations.get(name);
	}
}
