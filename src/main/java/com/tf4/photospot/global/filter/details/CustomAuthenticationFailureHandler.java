package com.tf4.photospot.global.filter.details;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.global.dto.ErrorResponse;
import com.tf4.photospot.global.exception.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		Throwable cause = exception.getCause();
		ApiException apiException;

		if (cause instanceof ApiException) {
			apiException = (ApiException)cause;
			String errorCode = apiException.getName();
			String errorMsg = apiException.getMessage();
			response.setStatus(apiException.getStatusCode().value());
			ErrorResponse errorResponse = ErrorResponse.builder()
				.code(errorCode)
				.message(errorMsg)
				.build();
			new ObjectMapper().writeValue(response.getWriter(), errorResponse);
		} else {
			ErrorResponse errorResponse = ErrorResponse.builder()
				.code(exception.getMessage())
				.build();
			new ObjectMapper().writeValue(response.getWriter(), errorResponse);
		}

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
	}
}
