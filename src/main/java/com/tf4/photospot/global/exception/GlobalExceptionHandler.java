package com.tf4.photospot.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tf4.photospot.global.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<?>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus())
			.body(ApiResponse.fail(ex.getErrorCode(), ex.getMessage()));
	}
}
