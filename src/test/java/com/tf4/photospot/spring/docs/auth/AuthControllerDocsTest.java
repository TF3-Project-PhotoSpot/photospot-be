package com.tf4.photospot.spring.docs.auth;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.presentation.AuthController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

import jakarta.servlet.http.Cookie;

public class AuthControllerDocsTest extends RestDocsSupport {

	private final AuthService authService = mock(AuthService.class);

	@Override
	protected Object initController() {
		return new AuthController(authService);
	}

	@DisplayName("액세스 토큰 재발급")
	@Test
	void reissueToken() throws Exception {
		// given
		var reissueResponse = new ReissueTokenResponse("new_access_token_value");
		given(authService.reissueToken(anyLong(), anyString())).willReturn(reissueResponse);

		// when & then
		mockMvc.perform(get("/api/v1/auth/reissue")
				.cookie(new Cookie("RefreshToken", "refresh_token_value"))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestCookies(
					cookieWithName("RefreshToken").description("리프레시 토큰")
				),
				responseFields(
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급 된 액세스 토큰")
				)));
	}
}
