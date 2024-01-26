package com.tf4.photospot.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.mockobject.WithCustomMockUser;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.application.UserService;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthControllerTest extends IntegrationTestSupport {
	private final WebApplicationContext context;
	private final JwtService jwtService;
	private final UserService userService;
	private MockMvc mockMvc;

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

	// // Todo : controller 테스트 삭제, service로 수정 (mock 써야함)
	// @Test
	// @WithCustomMockUser
	// @DisplayName("액세스 토큰 재발급을 성공한다.")
	// void reissueToken() throws Exception {
	// 	// given
	// 	var authentication = SecurityContextHolder.getContext().getAuthentication();
	// 	var user = (LoginUserDto)authentication.getPrincipal();
	// 	RefreshToken refreshToken = new RefreshToken(user.getId(), "refreshToken_value");
	// 	jwtRepository.save(refreshToken);
	//
	// 	// when & then
	// 	mockMvc.perform(get("/api/v1/auth/reissue")
	// 			.cookie(new Cookie("RefreshToken", "refreshToken_value"))
	// 		)
	// 		.andDo(print())
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.code").value(200))
	// 		.andExpect(jsonPath("$.message").value("OK"))
	// 		.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
	// }

	@Test
	@WithCustomMockUser
	@DisplayName("리프레시 토큰 없이 액세스 토큰 재발급을 요청하면 예외를 응답한다.")
	void reissueWithNoRefreshToken() throws Exception {
		mockMvc.perform(get("/api/v1/auth/reissue")
				.cookie(new Cookie("RefreshToken", "")))
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED_USER.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.UNAUTHORIZED_USER.getMessage()));
	}
}
