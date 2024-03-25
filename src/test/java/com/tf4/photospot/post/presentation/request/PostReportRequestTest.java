package com.tf4.photospot.post.presentation.request;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
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

public class PostReportRequestTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@ParameterizedTest
	@DisplayName("PostReportRequest 검증에 성공한다.")
	@MethodSource("provideValidPostReportRequest")
	void successValidCreatePostReportRequest(String reason) {
		PostReportRequest request = new PostReportRequest(reason);
		Set<ConstraintViolation<PostReportRequest>> violations = validator.validate(request);
		assertTrue(violations.isEmpty());
	}

	@ParameterizedTest
	@DisplayName("PostReportRequest 검증에 실패한다.")
	@MethodSource("provideInvalidPostReportRequest")
	void failInvalidCreatePostReportRequest(String reason) {
		PostReportRequest request = new PostReportRequest(reason);
		Set<ConstraintViolation<PostReportRequest>> violations = validator.validate(request);
		assertThat(violations.stream().findFirst().map(ConstraintViolation::getMessage).orElseThrow()).isEqualTo(
			"신고 사유는 200자 이하로 입력해주세요.");
	}

	private static Stream<Arguments> provideValidPostReportRequest() {
		String reason = createDummyStr(200);
		return Stream.of(Arguments.of(reason));
	}

	private static Stream<Arguments> provideInvalidPostReportRequest() {
		String reason = createDummyStr(201);
		return Stream.of(Arguments.of(reason));
	}
}
