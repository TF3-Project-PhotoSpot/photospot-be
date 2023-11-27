package com.tf4.photospot.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;

public class AuthServiceTest extends IntegrationTestSupport {

	@Autowired
	private AuthService authService;

	@DisplayName("사용자 로그인 및 토큰 발급 시나리오")
	@TestFactory
	Collection<DynamicTest> login() {
		//given
		String providerType = "kakao";
		String account = "account";

		return List.of(
			DynamicTest.dynamicTest("첫 로그인 유저의 토큰을 발급한다.", () -> {
				//when
				LoginTokenResponse tokenResponse = authService.login(providerType, account);

				//then
				assertAll(
					() -> assertThat(tokenResponse.hasLoggedInBefore()).isFalse(),
					() -> assertThat(tokenResponse.accessToken()).isNotBlank(),
					() -> assertThat(tokenResponse.refreshToken()).isNotBlank()
				);
			}),
			DynamicTest.dynamicTest("기존 로그인 유저의 토큰을 발급한다.", () -> {
				//when
				LoginTokenResponse tokenResponse2 = authService.login(providerType, account);

				//then
				assertAll(
					() -> assertThat(tokenResponse2.hasLoggedInBefore()).isTrue(),
					() -> assertThat(tokenResponse2.accessToken()).isNotBlank(),
					() -> assertThat(tokenResponse2.refreshToken()).isNotBlank()
				);
			})
		);
	}

}
