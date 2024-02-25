package com.tf4.photospot.auth.application;

import static com.tf4.photospot.global.config.jwt.JwtConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRedisRepository;
import com.tf4.photospot.global.config.jwt.JwtProperties;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtServiceTest extends IntegrationTestSupport {
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final JwtRedisRepository jwtRedisRepository;
	private final UserRepository userRepository;

	@DisplayName("리프레시 토큰을 발급하고 Redis에 저장한다.")
	@Test
	void issueTokens() {
		// given
		User userInfo = new User("nickname", "kakao", "account");
		User savedUser = userRepository.save(userInfo);
		String refreshToken = jwtService.issueRefreshToken(savedUser.getId());

		// when
		RefreshToken userRefreshToken = jwtRedisRepository.findByUserId(savedUser.getId()).orElseThrow();

		// then
		assertAll(
			() -> assertThat(userRefreshToken.getUserId()).isEqualTo(savedUser.getId()),
			() -> assertThat(userRefreshToken.getToken()).isEqualTo(refreshToken)
		);
	}

	@DisplayName("액세스 토큰 parse 시나리오")
	@TestFactory
	Collection<DynamicTest> parseAccessToken() {
		// given
		User user = new User("nickname", "kakao", "account");
		User savedUser = userRepository.save(user);
		String accessToken = jwtService.issueAccessToken(savedUser.getId(), savedUser.getRole().getType());

		return List.of(
			DynamicTest.dynamicTest("정상적인 액세스 토큰에서 값을 추출한다.", () -> {
				// given
				String authorizationHeader = PREFIX + accessToken;

				// when
				Claims claims = jwtService.parseAccessToken(authorizationHeader);

				// then
				assertAll(
					() -> assertThat(claims.get("id", Long.class)).isEqualTo(savedUser.getId()),
					() -> assertThat(claims.get("authorities", String.class)).isEqualTo(savedUser.getRole().getType())
				);
			}),
			DynamicTest.dynamicTest("액세스 토큰 값으로 null을 전달 받으면 예외를 발생한다.", () -> {
				// given
				String nullAccessToken = null;

				// when & then
				assertThatThrownBy(() -> jwtService.parseAccessToken(nullAccessToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			}),
			DynamicTest.dynamicTest("액세스 토큰 값이 Bearer로 시작하지 않으면 예외를 발생한다.", () -> {
				// given
				String noBearerAccessToken = "access token";

				// when & then
				assertThatThrownBy(() -> jwtService.parseAccessToken(noBearerAccessToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			}),
			DynamicTest.dynamicTest("조작된 토큰인 경우 예외를 발생한다.", () -> {
				// given
				String fakeAccessToken = PREFIX + "fake access token";

				// when & then
				assertThatThrownBy(() -> jwtService.parseAccessToken(fakeAccessToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.INVALID_ACCESS_TOKEN.getMessage());
			}),
			DynamicTest.dynamicTest("토큰이 만료된 경우 예외를 발생한다.", () -> {
				// given
				SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
				String expiredAccessToken = Jwts.builder()
					.claim(USER_ID, savedUser.getId())
					.claim(USER_AUTHORITIES, savedUser.getRole())
					.setIssuer(jwtProperties.getIssuer())
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis()))
					.signWith(key)
					.compact();

				// when & then
				assertThatThrownBy(() -> jwtService.parseAccessToken(PREFIX + expiredAccessToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.EXPIRED_ACCESS_TOKEN.getMessage());
			})
		);
	}

	@DisplayName("리프레시 토큰 parse 시나리오")
	@TestFactory
	Collection<DynamicTest> parseRefreshToken() {
		// given
		User user = new User("nickname", "kakao", "account");
		User savedUser = userRepository.save(user);
		String refreshToken = jwtService.issueRefreshToken(savedUser.getId());

		return List.of(
			DynamicTest.dynamicTest("정상적인 리프레시 토큰에서 값을 추출한다.", () -> {
				// when
				Claims claims = jwtService.parseRefreshToken(refreshToken);

				// then
				assertThat(claims.get("id", Long.class)).isEqualTo(savedUser.getId());
			}),
			DynamicTest.dynamicTest("리프레시 토큰 값으로 null을 전달 받으면 예외를 발생한다.", () -> {
				// given
				String nullRefreshToken = null;

				// when & then
				assertThatThrownBy(() -> jwtService.parseRefreshToken(nullRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			}),
			DynamicTest.dynamicTest("조작된 토큰인 경우 예외를 발생한다.", () -> {
				// given
				String fakeRefreshToken = "fake refresh token";

				// when & then
				assertThatThrownBy(() -> jwtService.parseRefreshToken(fakeRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
			}),
			DynamicTest.dynamicTest("토큰이 만료된 경우 예외를 발생한다.", () -> {
				// given
				SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
				String expiredRefreshToken = Jwts.builder()
					.claim(USER_ID, savedUser.getId())
					.setIssuer(jwtProperties.getIssuer())
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis()))
					.signWith(key)
					.compact();

				// when & then
				assertThatThrownBy(() -> jwtService.parseRefreshToken(expiredRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.EXPIRED_REFRESH_TOKEN.getMessage());
			})
		);
	}

	@DisplayName("리프레시 토큰 검증 시나리오")
	@TestFactory
	Collection<DynamicTest> validRefreshToken() {
		// given
		User user = new User("nickname", "kakao", "account");
		User savedUser = userRepository.save(user);
		String refreshToken = jwtService.issueRefreshToken(savedUser.getId());

		return List.of(
			DynamicTest.dynamicTest("DB에 저장된 토큰과 다를 경우 예외를 발생한다.", () -> {
				// given
				String wrongRefreshToken = "invalid refresh token";

				// when & then
				assertThatThrownBy(() -> jwtService.validRefreshToken(savedUser.getId(), wrongRefreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
			}),
			DynamicTest.dynamicTest("해당 사용자에 대한 토큰이 DB에 없는 경우 예외를 발생한다", () -> {
				// given
				jwtRedisRepository.deleteByUserId(savedUser.getId());

				// when & then
				assertThatThrownBy(() -> jwtService.validRefreshToken(savedUser.getId(), refreshToken))
					.isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			})
		);
	}
}
