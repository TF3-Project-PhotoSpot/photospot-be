package com.tf4.photospot.spring.docs.auth;

import static com.tf4.photospot.support.TestFixture.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.auth.presentation.AuthController;
import com.tf4.photospot.spring.docs.RestDocsSupport;
import com.tf4.photospot.user.application.UserService;

public class AuthControllerDocsTest extends RestDocsSupport {

	private final AuthService authService = mock(AuthService.class);
	private final UserService userService = mock(UserService.class);

	@Override
	protected Object initController() {
		return new AuthController(authService, userService);
	}

	@Test
	@DisplayName("액세스 토큰 재발급")
	void reissueToken() throws Exception {
		// given
		var reissueResponse = new ReissueTokenResponse("new_access_token_value", "new_refresh_token_value");
		given(authService.reissueToken(anyLong(), anyString())).willReturn(reissueResponse);

		// when & then
		mockMvc.perform(get("/api/v1/auth/reissue")
				.header("Authorization", "refresh_token_value")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestHeaders(
					headerWithName("Authorization").description("리프레시 토큰")
				),
				responseFields(
					fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급 된 액세스 토큰"),
					fieldWithPath("refreshToken").type(JsonFieldType.STRING).description("재발급 된 리프레시 토큰")
				)));
	}

	@Test
	@DisplayName("회원 탈퇴")
	void unlinkUser() throws Exception {
		// given
		given(userService.getActiveUser(anyLong())).willReturn(createUser("사용자"));

		// when
		mockMvc.perform(post("/api/v1/auth/unlink")
				.header("Authorization", "Bearer access_token"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestHeaders(headerWithName("Authorization").description("액세스 토큰")),
				queryParameters(parameterWithName("isLinked").description("현재 계정 연결 상태").optional()
					.attributes(defaultValue("true"))),
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공"))
			));
	}
}
