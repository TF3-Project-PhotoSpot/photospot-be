package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ApiErrorCode {

	NOT_FOUND_TAG(HttpStatus.BAD_REQUEST, "존재하지 않은 태그입니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
