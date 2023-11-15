package com.tf4.photospot.global.exception;

import org.springframework.http.HttpStatus;

public interface ApiErrorCode {
	HttpStatus getHttpStatus();

	String getMessage();
}
