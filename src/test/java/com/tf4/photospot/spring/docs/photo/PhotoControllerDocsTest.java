package com.tf4.photospot.spring.docs.photo;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
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

import com.tf4.photospot.global.util.S3Uploader;
import com.tf4.photospot.photo.application.PhotoService;
import com.tf4.photospot.photo.presentation.PhotoController;
import com.tf4.photospot.photo.presentation.response.PhotoSaveResponse;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class PhotoControllerDocsTest extends RestDocsSupport {

	private final PhotoService photoService = mock(PhotoService.class);
	private final S3Uploader s3Uploader = mock(S3Uploader.class);

	@Override
	protected Object initController() {
		return new PhotoController(photoService);
	}

	@Test
	@DisplayName("사진 업로드")
	void savePhoto() throws Exception {
		// given
		MockMultipartFile image = new MockMultipartFile("file", "image.jpg", "image/jpg", "<<image.jpg>>".getBytes());
		String requestContent = "{\"lon\" : 26.31, \"lat\" : 27.14, \"takenAt\" : \"2024-01-13T05:20:18.981+09:00\"}";
		MockMultipartFile request = new MockMultipartFile("request", "", "application/json", requestContent.getBytes());

		var response = new PhotoSaveResponse(1L);
		var photoUrl = "imageUrl";
		given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(photoUrl);
		given(photoService.save(any(MultipartFile.class), any(Point.class), any(LocalDate.class))).willReturn(response);

		// when & then
		mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/api/v1/photos")
				.file(image)
				.file(request)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestParts(
					partWithName("file").description("사용자가 업로드한 사진 파일"),
					partWithName("request").description("사용자가 업로드한 사진 정보")),
				requestPartFields("request",
					fieldWithPath("lon").description("사진의 경도"),
					fieldWithPath("lat").description("사진의 위도"),
					fieldWithPath("takenAt").description("사진 촬영일")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("photoId").type(JsonFieldType.NUMBER).description("업로드 성공한 사진 아이디")
				)
			));
	}
}
