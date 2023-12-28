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
	UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "unexpected error");

	private final HttpStatusCode statusCode;
	private final String message;
}
