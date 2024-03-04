package com.tf4.photospot.bookmark.presentation.request;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class CreateBookmarkFolderHttpRequestTest {
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@DisplayName("북마크 생성 검증에 성공한다.")
	@MethodSource("provideValidCreateBookmarkFolderHttpRequests")
	@ParameterizedTest
	void successValidCreateBookmarkFolderHttpRequest(CreateBookmarkFolderHttpRequest request) {
		Set<ConstraintViolation<CreateBookmarkFolderHttpRequest>> violations = validator.validate(request);
		violations.forEach(validation -> log.info(validation.getMessage()));
		assertTrue(violations.isEmpty());
	}

	@DisplayName("북마크 생성 검증에 실패한다.")
	@MethodSource("provideInValidCreateBookmarkFolderHttpRequests")
	@ParameterizedTest
	void failInvalidCreateBookmarkFolderHttpRequest(CreateBookmarkFolderHttpRequest request) {
		Set<ConstraintViolation<CreateBookmarkFolderHttpRequest>> violations = validator.validate(request);
		assertFalse(violations.isEmpty());
	}

	private static Stream<Arguments> provideValidCreateBookmarkFolderHttpRequests() {
		final StringBuilder maxDescriptionLength = new StringBuilder();
		IntStream.range(0, 30).forEach(i -> maxDescriptionLength.append("a"));
		StringBuilder maxNameLength = new StringBuilder();
		IntStream.range(0, 10).forEach(i -> maxNameLength.append("a"));

		return Stream.of(
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name("name").description("description").color("color").build()),
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name("a").description("description").color("color").build()),
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name(maxNameLength.toString()).description("description").color("color").build()),
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name("name").description("").color("color").build()),
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name("name").description(null).color("color").build()),
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name("name").description(maxDescriptionLength.toString()).color("color").build())
		);
	}

	private static Stream<Arguments> provideInValidCreateBookmarkFolderHttpRequests() {
		final StringBuilder maxDescriptionLength = new StringBuilder();
		IntStream.range(0, 30).forEach(i -> maxDescriptionLength.append("a"));
		StringBuilder maxNameLength = new StringBuilder();
		IntStream.range(0, 10).forEach(i -> maxNameLength.append("a"));

		return Stream.of(
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name(maxNameLength + "a").description("description").color("color").build()),
			Arguments.of(CreateBookmarkFolderHttpRequest.builder()
				.name("name").description(maxDescriptionLength + "a").color("").build())
		);
	}
}
