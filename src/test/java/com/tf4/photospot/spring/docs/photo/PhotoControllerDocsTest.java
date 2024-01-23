package com.tf4.photospot.spring.docs.photo;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.photo.application.PhotoService;
import com.tf4.photospot.photo.application.S3Uploader;
import com.tf4.photospot.photo.application.response.PhotoSaveResponse;
import com.tf4.photospot.photo.application.response.PhotoUploadResponse;
import com.tf4.photospot.photo.presentation.PhotoController;
import com.tf4.photospot.photo.presentation.request.PostPhotoSaveRequest;
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
		var image = new MockMultipartFile("file", "image.jpeg", "image/jpeg", "<<image.jpeg>>".getBytes());
		var photoUrl = "https://example.com/temp/image.jpeg";
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
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("photoUrl").type(JsonFieldType.STRING).description("S3 Temp 폴더에 업로드한 사진 URL")
				)
			));
	}

	@Test
	@DisplayName("사진 DB 저장")
	void savePhoto() throws Exception {
		// given
		var prePhotoUrl = "https://example.com/temp/image.jpeg";
		var request = new PostPhotoSaveRequest(prePhotoUrl, 26.31, 27.14, "2024-01-13T05:20:18.981+09:00");
		var postPhotoUrl = "https://example.com/post_images/image.jpeg";
		var response = new PhotoSaveResponse(1L);

		given(s3Uploader.moveFolder(anyString(), anyString())).willReturn(postPhotoUrl);
		given(photoService.save(anyString(), any(Point.class), any(LocalDate.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/photos")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("photoUrl").description("직전에 업로드한 사진 URL"),
					fieldWithPath("lon").description("경도"),
					fieldWithPath("lat").description("위도"),
					fieldWithPath("takenAt").description("사진이 찍힌 시각")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("photoId").type(JsonFieldType.NUMBER).description("저장 성공한 사진 아이디")
				)
			));
	}
}
