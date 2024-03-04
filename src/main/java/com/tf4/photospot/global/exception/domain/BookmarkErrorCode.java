package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkErrorCode implements ApiErrorCode {
	EXISTS_FOLDER_NAME(HttpStatus.BAD_REQUEST, "이미 존재하는 폴더가 있습니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
