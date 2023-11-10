package com.tf4.photospot.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ApiResponse<T>(
	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	String code,

	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	String message,

	@JsonInclude(value = JsonInclude.Include.NON_NULL)
	T data
) {
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(null, null, data);
	}

	public static ApiResponse<?> error(String code, String message) {
		return new ApiResponse<>(code, message, null);
	}
}
