package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlbumErrorCode implements ApiErrorCode {
	NO_AUTHORITY_ALBUM(HttpStatus.BAD_REQUEST, "앨범에 대한 권한이 없습니다."),
	CONTAINS_ALBUM_POSTS_CANNOT_DELETE(HttpStatus.BAD_REQUEST, "삭제할 수 없는 앨범 방명록이 포함되어 있습니다.");

	private final HttpStatusCode statusCode;
	private final String message;
}
