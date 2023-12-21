package com.tf4.photospot.global.config.maps;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.map.infrastructure.KakaoMapClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MapApiConfig {
	private static final String KAKAO_MAP_AUTHORIZATION_PREFIX = "KakaoAK ";

	private final KakaoMapProperties properties;

	@Bean
	public KakaoMapClient kakaoMapClient(RestClient.Builder restClientBuilder) {
		RestClient restClient = restClientBuilder
			.baseUrl(properties.getBaseUrl())
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.AUTHORIZATION, KAKAO_MAP_AUTHORIZATION_PREFIX + properties.getRestApiKey())
			.defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
				throw new ApiException(MapErrorCode.MAP_SERVER_ERROR);
			})
			.build();
		return HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(restClient))
			.build().createClient(KakaoMapClient.class);
	}
}
