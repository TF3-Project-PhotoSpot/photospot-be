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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.presentation.request.LoginDto;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.support.IntegrationTestSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthControllerTest extends IntegrationTestSupport {
	private final WebApplicationContext context;
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
}
