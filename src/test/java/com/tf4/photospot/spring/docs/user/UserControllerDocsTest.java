package com.tf4.photospot.spring.docs.user;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.spring.docs.RestDocsSupport;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.NicknameUpdateResponse;
import com.tf4.photospot.user.application.response.ProfileUpdateResponse;
import com.tf4.photospot.user.application.response.UserInfoResponse;
import com.tf4.photospot.user.presentation.UserController;
import com.tf4.photospot.user.presentation.request.NicknameUpdateRequest;

public class UserControllerDocsTest extends RestDocsSupport {

	private final UserService userService = mock(UserService.class);

	private final S3Uploader s3Uploader = mock(S3Uploader.class);

	@Override
	protected Object initController() {
		return new UserController(userService, s3Uploader);
	}

	@Test
	@DisplayName("프로필 수정")
	void updateProfile() throws Exception {
		// given
		var profile = new MockMultipartFile("file", "image.webp", "image/webp", "<<image.webp>>".getBytes());
		var imageUrl = "https://example.com/image.webp";
		var response = new ProfileUpdateResponse("https://bucket.s3.ap-northeast-2.amazonaws.com/profile/example.webp");

		given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(imageUrl);
		given(userService.updateProfile(anyLong(), anyString())).willReturn(response);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/v1/user/profile/me")
				.file(profile)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestParts(partWithName("file").description("사용자가 업로드한 프로필 사진")),
				responseFields(fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("업로드 성공한 이미지 URL"))
			));
	}

	@Test
	@DisplayName("닉네임 수정")
	void updateNickname() throws Exception {
		var request = new NicknameUpdateRequest("새로운_닉네임");
		given(userService.updateNickname(anyLong(), anyString())).willReturn(new NicknameUpdateResponse("새로운_닉네임"));

		mockMvc.perform(patch("/api/v1/user/nickname/me")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(fieldWithPath("nickname").type(JsonFieldType.STRING).description("바꾸려는 닉네임")),
				responseFields(fieldWithPath("nickname").type(JsonFieldType.STRING).description("바뀐 닉네임"))
			));
	}

	@Test
	@DisplayName("사용자 정보 조회")
	void getUserInfo() throws Exception {
		var response = new UserInfoResponse(1L, "사용자",
			"https://bucket.s3.ap-northeast-2.amazonaws.com/profile/example.webp", "kakao");
		given(userService.getInfo(anyLong())).willReturn(response);

		mockMvc.perform(get("/api/v1/user/me"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("userId").type(JsonFieldType.NUMBER).description("사용자 id"),
					fieldWithPath("nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
					fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("사용자 프로필 url"),
					fieldWithPath("provider").type(JsonFieldType.STRING).description("사용자 계정 OAuth 공급자")
				)
			));
	}
}
