package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ApiErrorCode {
	INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid Parameter"),
	UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected error"),
	CANNOT_SORTED_PROPERTY(HttpStatus.BAD_REQUEST, "정렬할 수 없는 property가 포함되어 있습니다."),
	MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "요청 파라미터를 추가해주세요."),
	FAILED_BECAUSE_OF_CONCURRENCY_UPDATE(HttpStatus.INTERNAL_SERVER_ERROR, "동시에 많은 업데이트로 인해 실패했습니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
