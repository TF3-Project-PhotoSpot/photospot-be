package com.tf4.photospot.global.dto;

public record ApiResponse<T>(
	String code,
	String message,
	T data
) {
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("200", "OK", data);
	}
}
