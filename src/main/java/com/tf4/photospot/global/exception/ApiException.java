package com.tf4.photospot.global.exception;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiException extends RuntimeException {
	private final ApiErrorCode errorStatus;

	public String getErrorCode() {
		return String.valueOf(errorStatus.getHttpStatus().value());
	}

	public HttpStatus getStatus() {
		return errorStatus.getHttpStatus();
	}

	public String getMessage() {
		return errorStatus.getMessage();
	}
}
