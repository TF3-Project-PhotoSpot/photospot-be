package com.tf4.photospot.global.filter.details;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.global.dto.ErrorResponse;
import com.tf4.photospot.global.exception.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ErrorResponseFactory {

	public static void create(HttpServletRequest request, HttpServletResponse response, ApiException exception) throws
		IOException {
		String errorCode = exception.getName();
		String errorMsg = exception.getMessage();
		response.setStatus(exception.getStatusCode().value());

		ErrorResponse errorResponse = ErrorResponse.builder()
			.code(errorCode)
			.message(errorMsg)
			.build();

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		new ObjectMapper().writeValue(response.getWriter(), errorResponse);
	}
}
