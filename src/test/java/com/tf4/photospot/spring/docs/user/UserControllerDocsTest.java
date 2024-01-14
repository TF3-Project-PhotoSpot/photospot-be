package com.tf4.photospot.spring.docs.user;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.util.S3Uploader;
import com.tf4.photospot.mockobject.WithCustomMockUser;
import com.tf4.photospot.spring.docs.RestDocsSupport;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.UserProfileResponse;
import com.tf4.photospot.user.presentation.UserController;

public class UserControllerDocsTest extends RestDocsSupport {

	private final UserService userService = mock(UserService.class);

	private final S3Uploader s3Uploader = mock(S3Uploader.class);

	@Override
	protected Object initController() {
		return new UserController(userService);
	}

	@Test
	@DisplayName("프로필 수정")
	@WithCustomMockUser
	void updateProfile() throws Exception {
		// given
		var profile = new MockMultipartFile("file", "image.jpg", "image/jpg", "<<image.jpg>>".getBytes());
		var requestContent = "{\"type\" : \"profile\"}";
		var request = new MockMultipartFile("request", "", "application/json", requestContent.getBytes());
		var imageUrl = "https://example.com/image.jpg";
		var response = new UserProfileResponse("imageUrl");

		given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(imageUrl);
		given(userService.updateProfile(anyLong(), any(MultipartFile.class), anyString())).willReturn(response);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/user/profile")
				.file(profile)
				.file(request)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestParts(
					partWithName("file").description("사용자가 업로드한 프로필 사진"),
					partWithName("request").description("사진 정보")),
				requestPartFields("request",
					fieldWithPath("type").description("프로필 사진에 대한 요청임을 명시")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("업로드 성공한 이미지 URL")
				)
			));
	}
}
