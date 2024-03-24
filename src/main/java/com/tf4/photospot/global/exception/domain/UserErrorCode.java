package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ApiErrorCode {

	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않거나 이미 탈퇴한 사용자입니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
