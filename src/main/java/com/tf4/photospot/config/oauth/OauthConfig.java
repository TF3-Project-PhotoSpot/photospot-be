package com.tf4.photospot.config.oauth;

import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tf4.photospot.auth.domain.oauth.OauthRegistration;
import com.tf4.photospot.auth.infrastructure.InMemoryRegistrationsRepository;
import com.tf4.photospot.auth.util.oauth.OauthAdapter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OauthProperties.class)
public class OauthConfig {

	private final OauthProperties properties;

	@Bean
	public InMemoryRegistrationsRepository createInMemoryRegistrationsRepository() {
		Map<String, OauthRegistration> registrations = OauthAdapter.createOauthRegistrations(properties);
		return new InMemoryRegistrationsRepository(registrations);
	}
}
