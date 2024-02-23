package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ApiErrorCode {
	INVALID_LOGIN_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 로그인 요청입니다."),
	EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
	EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
	PERMISSION_DENIED(HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다."),
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
	INVALID_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 권한입니다."),
	INVALID_PROVIDER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 공급자입니다."),
	UNEXPECTED_NICKNAME_GENERATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "닉네임 생성 중 예상치 못한 오류가 발생했습니다."),

	// kakao server
	KAKAO_AUTH_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Kakao 인증 서버 통신 중 오류가 발생했습니다."),
	INVALID_KAKAO_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 카카오 엑세스 토큰입니다."),
	EXPIRED_KAKAO_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "카카오 엑세스 토큰이 만료되었습니다."),

	// apple server
	APPLE_AUTH_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Apple ID 서버 통신 중 오류가 발생했습니다."),
	CRYPTO_KEY_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "암호화 중 오류가 발생했습니다."),
	INVALID_APPLE_IDENTIFY_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Apple ID 토큰입니다."),
	EXPIRED_APPLE_IDENTIFY_TOKEN(HttpStatus.UNAUTHORIZED, "Apple ID 토큰이 만료되었습니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
