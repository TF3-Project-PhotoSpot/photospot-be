package com.tf4.photospot.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.user.application.UserService;

import jakarta.servlet.http.Cookie;

public class AuthControllerTest extends IntegrationTestSupport {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserService userService;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(SecurityMockMvcConfigurers.springSecurity())
			.build();
	}

	@Test
	@DisplayName("로그인을 성공한다.")
	void login() throws Exception {
		// given
		var account = "account_value";
		var providerType = "kakao";

		// when & then
		mockMvc.perform(post("/api/v1/auth/login")
				.queryParam("account", account)
				.queryParam("providerType", providerType)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
			.andExpect(cookie().exists(JwtConstant.REFRESH_COOKIE_NAME));
	}

	@Test
	@DisplayName("로그인 시 잘못된 공급자를 받으면 예외를 응답한다.")
	void loginWithInvalidProviderType() throws Exception {
		// given
		var account = "account_value";
		var invalidProviderType = "koukou";

		// when & then
		mockMvc.perform(post("/api/v1/auth/login")
				.queryParam("account", account)
				.queryParam("providerType", invalidProviderType)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.INVALID_PROVIDER_TYPE.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_PROVIDER_TYPE.getMessage()));
	}

	@DisplayName("액세스 토큰 재발급을 성공한다.")
	@Test
	void reissueToken() throws Exception {
		// given
		var loginUser = userService.oauthLogin(OauthAttributes.KAKAO.getType(), "account_value");
		var refreshToken = jwtService.issueRefreshToken(loginUser.getId());

		// when & then
		mockMvc.perform(get("/api/v1/auth/reissue")
				.cookie(new Cookie("RefreshToken", refreshToken))
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
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
