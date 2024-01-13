package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3UploaderErrorCode implements ApiErrorCode {

	EMPTY_FILE(HttpStatus.BAD_REQUEST, "비어있는 파일입니다."),
	INVALID_PHOTO_EXTENSION(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 확장자입니다");

	private final HttpStatusCode statusCode;
	private final String message;
}
