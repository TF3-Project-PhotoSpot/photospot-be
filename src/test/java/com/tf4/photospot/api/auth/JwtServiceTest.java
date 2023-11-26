package com.tf4.photospot.api.auth;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.domain.jwt.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRepository;
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

}
