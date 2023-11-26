package com.tf4.photospot.auth.application;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.application.response.OauthTokenResponse;
import com.tf4.photospot.auth.application.response.UserLoginResponse;
import com.tf4.photospot.auth.domain.oauth.OauthRegistration;
import com.tf4.photospot.auth.domain.oauth.OauthUserInfo;
import com.tf4.photospot.auth.infrastructure.InMemoryRegistrationsRepository;
import com.tf4.photospot.user.application.UserService;

@Service
public class AuthService {

	private final InMemoryRegistrationsRepository inMemoryRegistrationsRepository;
	private final UserService userService;
	private final JwtService jwtService;
	private final RestClient restClient;

	public AuthService(InMemoryRegistrationsRepository inMemoryRegistrationsRepository, UserService userService,
		JwtService jwtService) {
		this.inMemoryRegistrationsRepository = inMemoryRegistrationsRepository;
		this.userService = userService;
		this.jwtService = jwtService;

		restClient = RestClient.create();
	}

	// Todo : 예외 수정
	@Transactional
	public LoginTokenResponse login(String code, String providerType) {
		OauthRegistration registration = inMemoryRegistrationsRepository.findByProviderType(providerType).orElseThrow();

		OauthTokenResponse tokenResponse = requestAccessToken(code, registration);
		OauthUserInfo userInfo = getUserInfo(providerType, registration, tokenResponse);

		UserLoginResponse loginUser = userService.oauthLogin(providerType, userInfo);
		return jwtService.issueTokens(loginUser.hasLoggedInBefore(),
			userService.findUser(loginUser.getAccount(), providerType));
	}

	private OauthTokenResponse requestAccessToken(String code, OauthRegistration registration) {
		return restClient.post()
			.uri(registration.getTokenUri())
			.headers(httpHeaders -> {
				httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
				httpHeaders.setAcceptCharset(List.of(StandardCharsets.UTF_8));
			})
			.body(createTokenRequestParams(code, registration))
			.retrieve()
			.toEntity(OauthTokenResponse.class)
			.getBody();
	}

	private MultiValueMap<String, String> createTokenRequestParams(String code, OauthRegistration registration) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", registration.getClientId());
		params.add("client_secret", registration.getClientSecret());
		params.add("redirect_uri", registration.getRedirectUri());
		params.add("code", code);
		return params;
	}

	private OauthUserInfo getUserInfo(String providerType, OauthRegistration registration,
		OauthTokenResponse tokenResponse) {
		Map<String, Object> userAttributes = getUserAttributes(registration, tokenResponse);
		return OauthUserInfo.of(providerType, userAttributes);
	}

	private Map<String, Object> getUserAttributes(OauthRegistration registration,
		OauthTokenResponse tokenResponse) {
		return restClient.get()
			.uri(registration.getUserInfoUri())
			.headers(header ->
				header.setBearerAuth(tokenResponse.accessToken()))
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}
}
