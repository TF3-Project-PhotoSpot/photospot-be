package com.tf4.photospot.photo.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;

public class FileUtilsTest {

	@TestFactory
	Stream<DynamicTest> generateNewFileNameTest() {
		return Stream.of(
			DynamicTest.dynamicTest("webp 확장자 추출을 성공한다.", () -> {
				var fileName = "https://example.com/image.webp";
				assertThat(FileUtils.extractExtension(fileName).getType()).isEqualTo("webp");
			}),
			DynamicTest.dynamicTest("jpeg 확장자 추출을 성공한다.", () -> {
				var fileName = "https://example.com/image.jpeg";
				assertThat(FileUtils.extractExtension(fileName).getType()).isEqualTo("jpeg");
			}),
			DynamicTest.dynamicTest("그 외 확장자는 예외를 던진다.", () -> {
				var fileName = "https://example.com/image.gif";
				assertThatThrownBy(() -> FileUtils.extractExtension(fileName))
					.isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION.getMessage());
			}),
			DynamicTest.dynamicTest("임의의 새로운 파일명을 만든다.", () -> {
				var fileName = "https://example.com/image.webp";
				assertTrue(FileUtils.generateNewFileName(fileName).matches("\\d{14}_\\w{8}.webp"));
			})
		);
	}
}
