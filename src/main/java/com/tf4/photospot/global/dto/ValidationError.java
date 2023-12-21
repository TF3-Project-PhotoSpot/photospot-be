package com.tf4.photospot.global.dto;

import org.springframework.validation.FieldError;

import lombok.Builder;

@Builder
public record ValidationError(
	String field,
	Object value,
	String message
) {
	public ValidationError {
		if (field == null) {
			field = "";
		}
		if (value == null) {
			value = "";
		}
		value = value.toString();
	}

	public static ValidationError from(FieldError fieldError) {
		return ValidationError.builder()
			.field(fieldError.getField())
			.value(fieldError.getRejectedValue())
			.message(fieldError.getDefaultMessage())
			.build();
	}
}
