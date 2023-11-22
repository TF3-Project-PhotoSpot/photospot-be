package com.tf4.photospot.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiException extends RuntimeException {
	private final ApiErrorCode errorCode;

	public HttpStatusCode getStatusCode() {
		return errorCode.getStatusCode();
	}

	public String getMessage() {
		return errorCode.getMessage();
	}

	public String getName() {
		return errorCode.name();
	}
}
