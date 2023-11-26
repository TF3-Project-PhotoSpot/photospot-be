package com.tf4.photospot.auth.infrastructure;

import java.util.Map;
import java.util.Optional;

import com.tf4.photospot.auth.domain.oauth.OauthRegistration;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryRegistrationsRepository {

	private final Map<String, OauthRegistration> registrations;

	public Optional<OauthRegistration> findByProviderType(String name) {
		return Optional.of(registrations.get(name));
	}
}
