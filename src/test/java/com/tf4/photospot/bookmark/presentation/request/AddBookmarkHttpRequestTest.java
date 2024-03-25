package com.tf4.photospot.bookmark.presentation.request;

import static com.tf4.photospot.support.TestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
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
class AddBookmarkHttpRequestTest {
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@DisplayName("AddBookmarkHttpRequests 검증에 성공한다.")
	@MethodSource("provideValidAddBookmarkHttpRequests")
	@ParameterizedTest(name = "spotId = {0}, name = {1}, description = {2}")
	void successValidCreateBookmarkFolderHttpRequest(Long spotId, String name, String description) {
		final AddBookmarkHttpRequest request = AddBookmarkHttpRequest.builder()
			.spotId(spotId)
			.name(name)
			.description(description)
			.build();
		Set<ConstraintViolation<AddBookmarkHttpRequest>> violations = validator.validate(request);
		violations.forEach(validation -> log.info(validation.getMessage()));
		assertTrue(violations.isEmpty());
	}

	@DisplayName("AddBookmarkHttpRequests 검증에 실패한다.")
	@MethodSource("provideInValidAddBookmarkHttpRequests")
	@ParameterizedTest(name = "spotId = {0}, name = {1}, description = {2}")
	void failInvalidCreateBookmarkFolderHttpRequest(Long spotId, String name, String description) {
		final AddBookmarkHttpRequest request = AddBookmarkHttpRequest.builder()
			.spotId(spotId)
			.name(name)
			.description(description)
			.build();
		Set<ConstraintViolation<AddBookmarkHttpRequest>> violations = validator.validate(request);
		assertFalse(violations.isEmpty());
	}

	private static Stream<Arguments> provideValidAddBookmarkHttpRequests() {
		final String maxNameLength = createDummyStr(10);
		final String maxDescriptionLength = createDummyStr(30);
		final Long spotId = 1L;
		return Stream.of(
			Arguments.of(spotId, "name", "description"),
			Arguments.of(spotId, maxNameLength, "description"),
			Arguments.of(spotId, "name", maxDescriptionLength),
			Arguments.of(spotId, "name", ""),
			Arguments.of(spotId, "name", null)
		);
	}

	private static Stream<Arguments> provideInValidAddBookmarkHttpRequests() {
		final String overMaxNameLength = createDummyStr(11);
		final String overMaxDescriptionLength = createDummyStr(31);
		return Stream.of(
			Arguments.of(-1L, "name", "description"),
			Arguments.of(0L, "name", "description"),
			Arguments.of(1L, overMaxNameLength, "description"),
			Arguments.of(1L, "name", overMaxDescriptionLength)
		);
	}
}
