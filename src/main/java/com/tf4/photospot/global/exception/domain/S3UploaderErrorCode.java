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
	INVALID_PHOTO_EXTENSION(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 확장자입니다"),
	UNEXPECTED_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류로 업로드를 실패했습니다."),
	UNEXPECTED_COPY_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류로 파일 복사를 실패했습니다."),
	UNEXPECTED_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류로 파일 삭제를 실패했습니다."),
	UNEXPECTED_GET_URL_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류로 이미지 URL 조회를 실패했습니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
