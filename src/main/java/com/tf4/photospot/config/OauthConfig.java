package com.tf4.photospot.config;

import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tf4.photospot.auth.domain.OauthAdapter;
import com.tf4.photospot.auth.domain.OauthProperties;
import com.tf4.photospot.auth.domain.OauthRegistration;
import com.tf4.photospot.auth.infrastructure.InMemoryRegistrationsRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {

	private final OauthProperties properties;

	@Bean
	public InMemoryRegistrationsRepository inMemoryProviderRepository() {
		Map<String, OauthRegistration> registrations = OauthAdapter.getOauthRegistrations(properties);
		return new InMemoryRegistrationsRepository(registrations);
	}
}
