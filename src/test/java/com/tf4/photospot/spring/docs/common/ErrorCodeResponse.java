package com.tf4.photospot.spring.docs.common;

public record ErrorCodeResponse(
	String type,
	String code,
	int status,
	String message
) {
}
