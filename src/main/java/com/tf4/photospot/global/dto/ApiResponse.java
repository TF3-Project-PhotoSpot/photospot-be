package com.tf4.photospot.global.dto;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
	String code,
	String message,
	List<ValidationError> errors
) {
	private static final String DEFAULT_SUCCESS_MESSAGE = "성공";
	public static final ApiResponse SUCCESS = new ApiResponse(null, DEFAULT_SUCCESS_MESSAGE, null);

	public ApiResponse {
		if (isErrorResponse(message) && errors == null) {
			errors = Collections.emptyList();
		}
	}

	public boolean isErrorResponse(String message) {
		return !DEFAULT_SUCCESS_MESSAGE.equals(message);
	}
}
