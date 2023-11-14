package com.tf4.photospot.global.config.maps;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("kakao.map")
public class KakaoMapProperties {
	private String restApiKey;
	private String baseUrl;
}
