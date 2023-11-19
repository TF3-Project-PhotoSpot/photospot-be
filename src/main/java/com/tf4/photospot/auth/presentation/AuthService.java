package com.tf4.photospot.auth.presentation;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.tf4.photospot.auth.domain.OauthRegistration;
import com.tf4.photospot.auth.domain.OauthUserInfo;
import com.tf4.photospot.auth.infrastructure.InMemoryRegistrationsRepository;
import com.tf4.photospot.auth.presentation.response.OauthTokenResponse;
import com.tf4.photospot.user.presentation.UserService;

@Service
public class AuthService {

	private final InMemoryRegistrationsRepository inMemoryRegistrationsRepository;
	private final UserService userService;
	private final RestClient restClient;

	public AuthService(InMemoryRegistrationsRepository inMemoryRegistrationsRepository, UserService userService) {
		this.inMemoryRegistrationsRepository = inMemoryRegistrationsRepository;
		this.userService = userService;

		restClient = RestClient.builder()
			.build();
	}

	// Todo : 예외 수정
	public void login(String code, String provider) {
		OauthRegistration registration = inMemoryRegistrationsRepository.findByProviderName(provider);

		if (registration == null) {
			throw new RuntimeException();
		}

		OauthTokenResponse tokenResponse = requestAccessToken(code, registration);
		OauthUserInfo userInfo = getUserInfo(provider, registration, tokenResponse);

		userService.oauthLogin(provider, userInfo);
	}

	private OauthTokenResponse requestAccessToken(String code, OauthRegistration registration) {
		return restClient.post()
			.uri(registration.getTokenUri())
			.headers(header -> {
				header.setBasicAuth(registration.getClientId(), registration.getClientSecret());
				header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
			})
			.body(createTokenRequestParams(code, registration))
			.retrieve()
			.body(OauthTokenResponse.class);
	}

	private MultiValueMap<String, String> createTokenRequestParams(String code, OauthRegistration registration) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("code", code);
		formData.add("grant_type", "authorization_code");
		formData.add("redirect_uri", registration.getRedirectUri());
		return formData;
	}

	private OauthUserInfo getUserInfo(String providerName, OauthRegistration registration,
		OauthTokenResponse tokenResponse) {
		Map<String, Object> userAttributes = getUserAttributes(registration, tokenResponse);
		return OauthUserInfo.of(providerName, userAttributes);
	}

	private Map<String, Object> getUserAttributes(OauthRegistration registration,
		OauthTokenResponse tokenResponse) {
		return restClient.get()
			.uri(registration.getUserInfoUri())
			.headers(header ->
				header.setBearerAuth(tokenResponse.getAccessToken()))
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}

}
