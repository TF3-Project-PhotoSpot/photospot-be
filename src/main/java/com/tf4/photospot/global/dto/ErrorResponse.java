package com.tf4.photospot.global.dto;

import java.util.Collections;
import java.util.List;

import lombok.Builder;

@Builder
public record ErrorResponse(
	String code,
	String message,
	List<ValidationError> errors
) {
	public ErrorResponse {
		if (errors == null) {
			errors = Collections.emptyList();
		}
	}
}
