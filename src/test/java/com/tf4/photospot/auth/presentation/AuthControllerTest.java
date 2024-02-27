package com.tf4.photospot.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.tf4.photospot.auth.infrastructure.JwtRedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.presentation.request.LoginDto;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.support.IntegrationTestSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthControllerTest extends IntegrationTestSupport {
	private final WebApplicationContext context;
	private final JwtRedisRepository jwtRedisRepository;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(SecurityMockMvcConfigurers.springSecurity())
			.build();
	}

	@Test
	@DisplayName("로그인 시 잘못된 공급자를 받으면 예외를 응답한다.")
	void loginWithInvalidProviderType() throws Exception {
		// given
		ObjectMapper objectMapper = new ObjectMapper();
		var account = "account_value";
		var invalidProviderType = "koukou";
		var loginDto = new LoginDto(invalidProviderType, account, "token");

		// when & then
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.INVALID_PROVIDER_TYPE.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_PROVIDER_TYPE.getMessage()));
	}

	@Test
	@DisplayName("리프레시 토큰 없이 액세스 토큰 재발급을 요청하면 예외를 응답한다.")
	void reissueWithNoRefreshToken() throws Exception {
		mockMvc.perform(get("/api/v1/auth/reissue"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED_USER.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.UNAUTHORIZED_USER.getMessage()));
	}

	@Test
	@DisplayName("인증되지 않은 사용자가 로그아웃을 요청하면 예외를 응답한다.")
	void logoutWithUnauthorizedUser() throws Exception {
		mockMvc.perform(delete("/api/v1/auth/logout")
				.header("Authorization", "invalid_access_token"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED_USER.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.UNAUTHORIZED_USER.getMessage()));
	}

	@Test
	@DisplayName("블랙리스트에 있는 액세스 토큰으로 로그아웃을 요청하면 예외를 응답한다.")
	void logoutWithAccessTokenInBlacklist() throws Exception {
		// given
		jwtRedisRepository.saveAccessTokenInBlackList("access_token_in_blacklist", 12345L);

		// when & then
		mockMvc.perform(delete("/api/v1/auth/logout")
				.header("Authorization", "access_token_in_blacklist"))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.INVALID_ACCESS_TOKEN.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_ACCESS_TOKEN.getMessage()));
	}
}
