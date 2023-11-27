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

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRepository;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserRepository;

public class JwtServiceTest extends IntegrationTestSupport {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private JwtRepository jwtRepository;

	@Autowired
	private UserRepository userRepository;

	@DisplayName("사용자 JWT 토큰을 발급하고 Refresh Token을 DB에 저장한다.")
	@Test
	void issueTokens() {
		// given
		boolean hasLoggedInBefore = false;
		User userInfo = new User("nickname", "kakao", "account");
		User savedUser = userRepository.save(userInfo);
		LoginTokenResponse tokenResponse = jwtService.issueTokens(hasLoggedInBefore, savedUser);

		// when
		RefreshToken refreshToken = jwtRepository.findByUserId(savedUser.getId()).orElseThrow();

		// then
		assertAll(
			() -> assertThat(refreshToken.getUserId()).isEqualTo(savedUser.getId()),
			() -> assertThat(refreshToken.getToken()).isEqualTo(tokenResponse.refreshToken())
		);
	}

	@DisplayName("액세스 토큰 재발급 시나리오")
	@TestFactory
	Collection<DynamicTest> validRefreshToken() {
		// given
		boolean hasLoggedInBefore = false;
		User user = new User("nickname", "kakao", "account");
		User savedUser = userRepository.save(user);
		LoginTokenResponse tokenResponse = jwtService.issueTokens(hasLoggedInBefore, savedUser);

		return List.of(
			DynamicTest.dynamicTest("리프레시 토큰 값으로 null을 전달 받으면 예외를 발생한다.", () -> {
				// given
				String wrongRefreshToken = "";

				// when & then
				assertThatThrownBy(() -> jwtService.validRefreshToken(savedUser.getId(), wrongRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			}),
			DynamicTest.dynamicTest("Bearer PREFIX가 없으면 예외를 발생한다.", () -> {
				// given
				String wrongRefreshToken = "refresh_token";

				// when & then
				assertThatThrownBy(() -> jwtService.validRefreshToken(savedUser.getId(), wrongRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			}),
			DynamicTest.dynamicTest("DB에 저장된 토큰과 다를 경우 예외를 발생한다.", () -> {
				// given
				String wrongRefreshToken = "Bearer expired_refresh_token";

				// when & then
				assertThatThrownBy(() -> jwtService.validRefreshToken(savedUser.getId(), wrongRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
			}),
			DynamicTest.dynamicTest("토큰이 만료된 경우 예외를 발생한다.", () -> {
				// given
				String refreshToken = "Bearer " + tokenResponse.refreshToken();
				jwtService.removeRefreshToken(savedUser.getId());

				// when & then
				assertThatThrownBy(() -> jwtService.validRefreshToken(savedUser.getId(), refreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.EXPIRED_TOKEN.getMessage());
			})
		);
	}

}
