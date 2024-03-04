package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkErrorCode implements ApiErrorCode {
	EXISTS_FOLDER_NAME(HttpStatus.BAD_REQUEST, "이미 존재하는 폴더가 있습니다."),
	INVALID_BOOKMARK_FOLDER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 폴더 id 입니다."),
	NO_AUTHORITY_BOOKMARK_FOLDER(HttpStatus.FORBIDDEN, "폴더에 대한 권한이 없습니다."),
	MAX_BOOKMARKED(HttpStatus.FORBIDDEN, "현재 폴더의 북마크 개수가 최대입니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
