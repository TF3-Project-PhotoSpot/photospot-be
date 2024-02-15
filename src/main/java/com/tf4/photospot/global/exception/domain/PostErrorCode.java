package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ApiErrorCode {

	NOT_FOUND_TAG(HttpStatus.NOT_FOUND, "존재하지 않는 태그입니다."),
	NOT_FOUND_POST(HttpStatus.NOT_FOUND, "존재하지 않는 방명록입니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
