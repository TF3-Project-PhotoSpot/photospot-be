package com.tf4.photospot.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.user.application.UserService;

@Transactional
public class AuthServiceTest extends IntegrationTestSupport {

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserService userService;

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

	@DisplayName("DB에 저장된 리프레시 토큰과 전달 받은 토큰이 같으면 액세스 토큰을 재발급 한다.")
	@Test
	void reissue() {
		// given
		String providerType = "kakao";
		String account = "account";
		String refreshToken = "Bearer " + authService.login(providerType, account).refreshToken();

		// when
		ReissueTokenResponse tokenResponse = authService.reissueToken(refreshToken);

		// then
		assertThat(tokenResponse.accessToken()).isNotBlank();
	}

}
