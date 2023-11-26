package com.tf4.photospot.spring.docs.auth;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.auth.application.AuthController;
import com.tf4.photospot.auth.presentation.AuthService;
import com.tf4.photospot.auth.presentation.response.LoginTokenResponse;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class AuthControllerDocsTest extends RestDocsSupport {

	private final AuthService authService = mock(AuthService.class);

	@Override
	protected Object initController() {
		return new AuthController(authService);
	}

	@DisplayName("카카오 최초 로그인")
	@Test
	void initialLoginByKakao() throws Exception {

		//given
		var initialLoginUserResponse
			= new LoginTokenResponse(false, "access_token", "refresh_token");

		given(authService.login(any(String.class), any(String.class)))
			.willReturn(initialLoginUserResponse);

		//when & then
		mockMvc.perform(get("/api/v1/auth/login/kakao")
				.queryParam("code", "authorization_code")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("initial-login",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("code").description("인가 코드")
				),
				responseFields(
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
					fieldWithPath("data.hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("이전 로그인 여부"),
					fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
					fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
				)));
	}

	@DisplayName("기존 유저 카카오 로그인")
	@Test
	void previousLoginByKakao() throws Exception {

		// given
		var previousLoginUserResponse
			= new LoginTokenResponse(true, "access_token", "refresh_token");

		given(authService.login(any(String.class), any(String.class)))
			.willReturn(previousLoginUserResponse);

		// when & then
		mockMvc.perform(get("/api/v1/auth/login/kakao")
				.queryParam("code", "authorization_code")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("previous-login",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("code").description("인가 코드")
				),
				responseFields(
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
					fieldWithPath("data.hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("이전 로그인 유무"),
					fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
					fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
				)));
	}
}