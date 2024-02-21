package com.tf4.photospot.spring.docs.auth;

import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
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
		// given
		LoginRequest request = new LoginRequest("kakao", "user_id");

		// when
		mockMvc.perform(post("/api/v1/auth/login")
				.header("Authorization", "Bearer jwt_from_oauth_server")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
				.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestHeaders(
					headerWithName("Authorization").description("카카오 access token 또는 애플 identity token")
				),
				requestFields(
					fieldWithPath("providerType").type(JsonFieldType.STRING)
						.description("OAuth 공급자명('kakao' 또는 'apple')"),
					fieldWithPath("identifier").type(JsonFieldType.STRING).description("카카오 회원번호 또는 애플 nonce 값")
				),
				responseCookies(
					cookieWithName(JwtConstant.REFRESH_COOKIE_NAME).description("리프레시 토큰")
				),
				responseFields(
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("애플리케이션 서버용 액세스 토큰"),
					fieldWithPath("hasLoggedInBefore").type(JsonFieldType.BOOLEAN).description("최초 로그인 여부")
				)
			));
	}
}
