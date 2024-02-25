package com.tf4.photospot.spring.docs.auth;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.spring.docs.RestDocsSupport;

public class SecurityControllerDocsTest extends RestDocsSupport {

	@Override
	protected Object initController() {
		return new SecurityController();
	}

	@Test
	@DisplayName("로그인")
	void login() throws Exception {
		// given
		LoginRequest request = new LoginRequest("kakao", "user_id", "token_from_oauth_server");

		// when
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
				.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("providerType").type(JsonFieldType.STRING)
						.description("OAuth 공급자명('kakao' 또는 'apple')"),
					fieldWithPath("identifier").type(JsonFieldType.STRING).description("카카오 회원번호 또는 애플 nonce 값"),
					fieldWithPath("token").type(JsonFieldType.STRING)
						.description("카카오 access token 또는 애플 identify token")
				),
				responseFields(
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("애플리케이션 서버용 access token"),
					fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("애플리케이션 서버용 refresh token"),
					fieldWithPath("hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("최초 로그인 여부")
				)
			));
	}
}
