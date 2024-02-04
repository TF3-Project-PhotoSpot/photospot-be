package com.tf4.photospot.spring.docs.photo;

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

import com.tf4.photospot.photo.application.PhotoService;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.photo.application.response.PhotoUploadResponse;
import com.tf4.photospot.photo.presentation.PhotoController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class PhotoControllerDocsTest extends RestDocsSupport {

	private final PhotoService photoService = mock(PhotoService.class);
	private final S3Uploader s3Uploader = mock(S3Uploader.class);

	@Override
	protected Object initController() {
		return new PhotoController(photoService);
	}

	@Test
	@DisplayName("사진 S3 업로드")
	void uploadPhoto() throws Exception {
		// given
		var image = new MockMultipartFile("file", "image.webp", "image/webp", "<<image.webp>>".getBytes());
		var photoUrl = "https://example.com/temp/image.webp";
		var response = new PhotoUploadResponse(photoUrl);

		given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(photoUrl);
		given(photoService.upload(any(MultipartFile.class))).willReturn(response);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/photos/s3")
				.file(image)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestParts(
					partWithName("file").description("사용자가 업로드한 사진 파일")
				),
				responseFields(
					fieldWithPath("photoUrl").type(JsonFieldType.STRING).description("S3 Temp 폴더에 업로드한 사진 URL")
				)
			));
	}
}
