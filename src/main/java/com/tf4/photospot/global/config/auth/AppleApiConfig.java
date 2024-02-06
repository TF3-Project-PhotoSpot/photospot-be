package com.tf4.photospot.global.config.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.tf4.photospot.auth.infrastructure.AppleClient;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AppleApiConfig {

	@Bean
	public AppleClient appleClient(RestClient.Builder restClientBuilder) {
		RestClient restClient = restClientBuilder
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
				throw new ApiException(AuthErrorCode.APPLE_ID_SERVER_ERROR);
			})
			.build();
		return HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(restClient))
			.build().createClient(AppleClient.class);
	}
}
