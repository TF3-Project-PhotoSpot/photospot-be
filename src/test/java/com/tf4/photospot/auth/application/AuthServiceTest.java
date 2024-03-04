package com.tf4.photospot.auth.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRedisRepository;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.mockobject.WithCustomMockUser;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthServiceTest extends IntegrationTestSupport {
	private final AuthService authService;
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final JwtRedisRepository jwtRedisRepository;

	@TestFactory
	@DisplayName("사용자 등록 시나리오")
	Collection<DynamicTest> saveUser() {
		// given
		String providerType = "kakao";
		String account = "account_value";

		return List.of(
			dynamicTest("최초 로그인 시 사용자 정보를 DB에 저장한다.", () -> {
				//when
				var loginResponse = authService.oauthLogin(providerType, account);

				//then
				assertAll(
					() -> assertThat(loginResponse.hasLoggedInBefore()).isFalse(),
					() -> assertThat(userRepository.findAll()).hasSize(1)
				);
			}),
			dynamicTest("기존에 로그인 했던 사용자는 DB에 저장하지 않는다.", () -> {
				//when
				var loginResponse2 = authService.oauthLogin(providerType, account);
				User savedUser = userRepository.findUserByProviderTypeAndAccount(providerType, account).orElseThrow();

				//then
				assertAll(
					() -> assertThat(loginResponse2.hasLoggedInBefore()).isTrue(),
					() -> assertThat(userRepository.findAll()).hasSize(1),
					() -> assertThat(savedUser.getAccount()).isEqualTo(account)
				);
			})
		);
	}

	@Test
	@WithCustomMockUser
	@DisplayName("DB에 저장된 리프레시 토큰과 전달 받은 토큰이 같으면 액세스 토큰을 재발급 한다.")
	void reissue() {
		// given
		Long userId = userRepository.save(createUser("사용자")).getId();
		String refreshToken = jwtService.issueRefreshToken(userId);

		// when
		ReissueTokenResponse tokenResponse = authService.reissueToken(userId, refreshToken);

		// then
		assertThat(tokenResponse.accessToken()).isNotBlank();
	}

	@TestFactory
	@WithCustomMockUser
	Stream<DynamicTest> logout() {
		// given
		User loginUser = createUser("사용자");
		userRepository.save(loginUser);
		jwtRedisRepository.save(new RefreshToken(loginUser.getId(), "refresh_token"));
		String accessToken = "Bearer " + jwtService.issueAccessToken(loginUser.getId(), loginUser.getRole().getType());

		return Stream.of(
			dynamicTest("로그아웃을 하면 Redis에 저장된 리프레시 토큰을 삭제하고 블랙리스트에 액세스 토큰을 추가한다.", () -> {
				// when
				authService.logout(accessToken);

				// then
				assertAll(
					() -> assertThat(jwtRedisRepository.findByUserId(loginUser.getId())).isEqualTo(Optional.empty()),
					() -> assertThatThrownBy(() -> authService.existsBlacklist(accessToken))
						.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.INVALID_ACCESS_TOKEN.getMessage())
				);
			}),
			dynamicTest("유효하지 않은 액세스 토큰으로 로그아웃 요청 시 예외를 던진다.", () -> {
				// given
				String wrongAccessToken = "wrong_access_token";

				// when & then
				assertThatThrownBy(() -> authService.logout(wrongAccessToken))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.UNAUTHORIZED_USER.getMessage());
			})
		);
	}
}
