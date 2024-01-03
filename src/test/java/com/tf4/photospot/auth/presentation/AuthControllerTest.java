package com.tf4.photospot.auth.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.config.security.SecurityConfig;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.OauthLoginUserResponse;
import com.tf4.photospot.user.domain.Role;

import jakarta.servlet.http.Cookie;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@MockBean
	private AuthService authService;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private UserService userService;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(webApplicationContext)
			.apply(SecurityMockMvcConfigurers.springSecurity())
			.build();
	}

	@Test
	@DisplayName("로그인 성공 테스트")
	void login() throws Exception {
		// given
		String account = "account";
		String providerType = "kakao";

		var mockUser = new OauthLoginUserResponse(false, 1L, Role.USER);
		Mockito.when(userService.oauthLogin(providerType, account)).thenReturn(mockUser);

		String accessToken = "access token";
		Mockito.when(jwtService.issueAccessToken(mockUser.getId(), mockUser.getRole().type)).thenReturn(accessToken);
		String refreshToken = "refresh token";
		Mockito.when(jwtService.issueRefreshToken(mockUser.getId())).thenReturn(refreshToken);

		mockMvc.perform(post("/api/v1/auth/login")
				.queryParam("account", account)
				.queryParam("providerType", providerType)
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data.accessToken").value(accessToken))
			.andExpect(jsonPath("$.data.hasLoggedInBefore").value(false))
			.andExpect(cookie().value(JwtConstant.REFRESH_COOKIE_NAME, refreshToken));
	}

	@Test
	@DisplayName("잘못된 공급자를 받으면 예외를 응답한다.")
	void loginWithInvalidProviderType() throws Exception {
		// given
		String account = "account";
		String providerType = "kekeo";

		mockMvc.perform(post("/api/v1/auth/login")
				.queryParam("account", account)
				.queryParam("providerType", providerType)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(AuthErrorCode.INVALID_PROVIDER_TYPE.name()))
			.andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_PROVIDER_TYPE.getMessage()));
	}

	@Test
	@DisplayName("액세스 토큰 재발급")
	void reissueAccessTokenWithRefreshToken() throws Exception {
		// given
		String refreshToken = "refresh token";
		String newAccessToken = "new access token";

		Mockito.when(authService.reissueToken(refreshToken)).thenReturn(new ReissueTokenResponse(newAccessToken));

		mockMvc.perform(get("/api/v1/auth/reissue")
				.cookie(new Cookie("RefreshToken", refreshToken)))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value(200))
			.andExpect(jsonPath("$.message").value("OK"))
			.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
	}

}
