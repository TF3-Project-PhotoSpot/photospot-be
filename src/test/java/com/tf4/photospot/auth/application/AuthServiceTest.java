package com.tf4.photospot.auth.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.user.application.UserService;

public class AuthServiceTest extends IntegrationTestSupport {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserService userService;

	@Autowired
	private JwtService jwtService;

	@DisplayName("DB에 저장된 리프레시 토큰과 전달 받은 토큰이 같으면 액세스 토큰을 재발급 한다.")
	@Test
	void reissue() {
		// given
		String providerType = "kakao";
		String account = "account";
		Long loginUser = userService.oauthLogin(providerType, account).getId();
		String refreshToken = jwtService.issueRefreshToken(loginUser);

		// when
		ReissueTokenResponse tokenResponse = authService.reissueToken(loginUser, refreshToken);

		// then
		assertThat(tokenResponse.accessToken()).isNotBlank();
	}

}
