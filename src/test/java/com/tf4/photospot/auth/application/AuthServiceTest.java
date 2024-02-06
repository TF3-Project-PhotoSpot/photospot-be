package com.tf4.photospot.auth.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.support.IntegrationTestSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthServiceTest extends IntegrationTestSupport {
	private final AuthService authService;
	private final JwtService jwtService;

	@DisplayName("DB에 저장된 리프레시 토큰과 전달 받은 토큰이 같으면 액세스 토큰을 재발급 한다.")
	@Test
	void reissue() {
		// given
		String providerType = "kakao";
		Map<String, String> identityInfo = Map.of(SecurityConstant.ACCOUNT_PARAM, "account_value");
		Long loginUser = authService.oauthLogin(providerType, identityInfo).getId();
		String refreshToken = jwtService.issueRefreshToken(loginUser);

		// when
		ReissueTokenResponse tokenResponse = authService.reissueToken(loginUser, refreshToken);

		// then
		assertThat(tokenResponse.accessToken()).isNotBlank();
	}

}
