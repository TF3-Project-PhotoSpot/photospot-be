package com.tf4.photospot.photo.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.support.IntegrationTestSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Import(MockS3Config.class)
public class PhotoServiceTest extends IntegrationTestSupport {

	private final PhotoService photoService;
	private final MockS3Config mockS3Config;

	@TestFactory
	@DisplayName("사진 업로드 시나리오")
	Collection<DynamicTest> uploadPhoto() {
		// given
		var file = new MockMultipartFile("file", "test.webp", "image/webp", "<<webp data>>".getBytes());

		return List.of(
			dynamicTest("사진 업로드에 성공한다", () -> {
				// when
				String photoUrl = photoService.upload(file).photoUrl();

				// then
				assertThat(photoUrl).isEqualTo(mockS3Config.getDummyUrl());
			}),
			dynamicTest("유효하지 않은 확장자 파일을 받으면 예외를 던진다.", () -> {
				// given
				var gifFile = new MockMultipartFile("gif_file", "moving.gif", "image/gif", "<<gif data>>".getBytes());

				// when & then
				assertThatThrownBy(() -> photoService.upload(gifFile)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION.getMessage());
			}),
			dynamicTest("비어있는 파일을 받으면 예외를 던진다.", () -> {
				// given
				var emptyFile = new MockMultipartFile("empty_file", "empty.webp", "image/webp", new byte[0]);

				// when & then
				assertThatThrownBy(() -> photoService.upload(emptyFile)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.EMPTY_FILE.getMessage());
			}),
			dynamicTest("파일명이 유효하지 않으면 예외를 던진다.", () -> {
				// given
				var invalidFile = new MockMultipartFile("invalid_file", null, "image/webp", "<<webp data>>".getBytes());

				// when & then
				assertThatThrownBy(() -> photoService.upload(invalidFile)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.INVALID_FILE_NAME.getMessage());
			})
		);
	}
}
