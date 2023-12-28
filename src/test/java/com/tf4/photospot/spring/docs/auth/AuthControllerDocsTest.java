package com.tf4.photospot.spring.docs.auth;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.presentation.AuthController;
import com.tf4.photospot.auth.presentation.request.ReissueRequest;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class AuthControllerDocsTest extends RestDocsSupport {

	private final AuthService authService = mock(AuthService.class);

	@Override
	protected Object initController() {
		return new AuthController(authService);
	}

	@DisplayName("최초 로그인")
	@Test
	void initialLogin() throws Exception {
		//given
		var initialLoginUserResponse
			= new LoginTokenResponse(false, "access_token", "refresh_token");

		given(authService.login(any(String.class), any(String.class)))
			.willReturn(initialLoginUserResponse);

		//when & then
		mockMvc.perform(get("/api/v1/auth/login")
				.queryParam("providerType", "kakao")
				.queryParam("account", "123456")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("providerType").description("로그인 공급자 타입"),
					parameterWithName("account").description("oauth에서 제공하는 사용자 고유 account")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("이전 로그인 여부"),
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
					fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
				)));
	}

	@DisplayName("기존 유저 로그인")
	@Test
	void previousLogin() throws Exception {
		// given
		var previousLoginUserResponse
			= new LoginTokenResponse(true, "access_token", "refresh_token");

		given(authService.login(any(String.class), any(String.class)))
			.willReturn(previousLoginUserResponse);

		// when & then
		mockMvc.perform(get("/api/v1/auth/login")
				.queryParam("providerType", "kakao")
				.queryParam("account", "123456")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("providerType").description("로그인 공급자 타입"),
					parameterWithName("account").description("oauth에서 제공하는 사용자 고유 account")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("이전 로그인 유무"),
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
					fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
				)));
	}

	@DisplayName("액세스 토큰 재발급")
	@Test
	void reissueToken() throws Exception {
		// given
		var tokenResponse = new ReissueTokenResponse("access_token");
		given(authService.reissueToken(any(String.class))).willReturn(tokenResponse);

		// when & then
		mockMvc.perform(get("/api/v1/auth/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new ReissueRequest("refresh_token")))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("사용자의 리프레시 토큰")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급 된 액세스 토큰")
				)));
	}
}
