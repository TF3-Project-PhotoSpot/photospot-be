package com.tf4.photospot.support;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

import com.tf4.photospot.global.config.maps.KakaoMapProperties;

@EnableConfigurationProperties(value = KakaoMapProperties.class)
@TestPropertySource("classpath:application.yml")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RestClientTest
public abstract class RestClientTestSupport {
}
