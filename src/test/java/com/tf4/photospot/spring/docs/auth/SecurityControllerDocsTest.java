package com.tf4.photospot.spring.docs.auth;

import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class SecurityControllerDocsTest extends RestDocsSupport {

	@Override
	protected Object initController() {
		return new SecurityController();
	}

	@Test
	@DisplayName("로그인")
	void login() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.queryParam("account", "kakao_account")
				.queryParam("providerType", "kakao")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("account").description("oauth 계정"),
					parameterWithName("providerType").description("oauth 공급자")
				),
				responseCookies(
					cookieWithName(JwtConstant.REFRESH_COOKIE_NAME).description("리프레시 토큰")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
					fieldWithPath("hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("최초 로그인 여부")
				)
			));
	}
}
