package com.tf4.photospot.global.exception;

import org.springframework.http.HttpStatusCode;

public interface ApiErrorCode {
	HttpStatusCode getStatusCode();

	String getMessage();

	String name();
}
