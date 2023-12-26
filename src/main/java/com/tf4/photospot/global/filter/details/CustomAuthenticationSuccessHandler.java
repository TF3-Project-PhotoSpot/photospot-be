package com.tf4.photospot.global.filter.details;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.response.LoginResponse;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.LoginUserDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtGenerator jwtGenerator;

	// Todo : 상수 처리
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		LoginUserDto loginUser = getOauthUserFromAuthentication(authentication);
		jwtGenerator.generate(response, loginUser, authentication.getAuthorities());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		new ObjectMapper().writeValue(response.getWriter(), createBody(loginUser.hasLoggedInBefore()));
	}

	// Todo : 예외 변경
	private LoginUserDto getOauthUserFromAuthentication(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof LoginUserDto) {
			return (LoginUserDto)authentication.getPrincipal();
		}
		throw new RuntimeException("invalid authentication");
	}

	private ApiResponse<LoginResponse> createBody(Boolean hasLoggedInBefore) {
		return ApiResponse.success(new LoginResponse(hasLoggedInBefore));
	}
}
