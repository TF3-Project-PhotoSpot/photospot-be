package com.tf4.photospot.global.config.maps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.tf4.photospot.spot.infrastructure.KakaoMapHttpExchange;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MapApiConfig {
	private static final String KAKAO_MAP_AUTHORIZATION_PREFIX = "KakaoAK ";

	private final KakaoMapProperties properties;

	@Bean
	KakaoMapHttpExchange kakaoMapHttpExchange() {
		var restClient = RestClient.builder()
			.baseUrl(properties.getBaseUrl())
			.defaultHeader(HttpHeaders.AUTHORIZATION, KAKAO_MAP_AUTHORIZATION_PREFIX + properties.getRestApiKey())
			.build();
		return HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(restClient))
			.build()
			.createClient(KakaoMapHttpExchange.class);
	}
}
