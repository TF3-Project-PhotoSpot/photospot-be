package com.tf4.photospot.global.exception;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tf4.photospot.global.dto.ApiResponse;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponse<?>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.error(ex.getName(), ex.getMessage()));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		@NonNull MethodArgumentNotValidException ex,
		@NotNull HttpHeaders headers,
		@NotNull HttpStatusCode status,
		@NotNull WebRequest request) {
		return ResponseEntity.status(status)
			.body(ApiResponse.error(String.valueOf(status.value()), joiningFieldErrorMessage(ex)));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<?>> test() {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
			.body(ApiResponse.error(HttpStatus.BAD_REQUEST.name(), "Invalid Parameter"));
	}

	private static String joiningFieldErrorMessage(MethodArgumentNotValidException ex) {
		return ex.getFieldErrors().stream()
			.collect(Collectors.toMap(
				FieldError::getField,
				filedError -> Optional.ofNullable(filedError.getDefaultMessage())
					.orElseGet(() -> "Invalid Parameter")
			)).toString();
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
		Exception ex,
		Object body,
		@NotNull HttpHeaders headers,
		@NotNull HttpStatusCode statusCode,
		@NotNull WebRequest request) {
		return ResponseEntity.status(statusCode)
			.body(ApiResponse.error(String.valueOf(statusCode.value()), ex.getMessage()));
	}
}
