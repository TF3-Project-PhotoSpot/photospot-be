package com.tf4.photospot.global.exception.domain;

import org.springframework.http.HttpStatus;

import com.tf4.photospot.global.exception.ApiErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MapErrorCode implements ApiErrorCode {
	NO_ADDRESS_FOR_GIVEN_COORD(HttpStatus.NOT_FOUND, "해당 좌표의 주소를 찾을 수 없습니다."),
	NO_COORD_FOR_GIVEN_ADDRESS(HttpStatus.NOT_FOUND, "해당 주소의 좌표를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
