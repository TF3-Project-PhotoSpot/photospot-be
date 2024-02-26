package com.tf4.photospot.global.filter;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomLogoutFilter extends OncePerRequestFilter {
	private final AuthService authService;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws IOException {
		String accessToken = request.getHeader(JwtConstant.AUTHORIZATION_HEADER);
		checkBlacklist(accessToken);
		authService.logout(accessToken);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "성공")));
	}

	private void checkBlacklist(String accessToken) {
		if (authService.existsBlacklist(accessToken)) {
			throw new ApiException(AuthErrorCode.INVALID_ACCESS_TOKEN);
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().equals(SecurityConstant.LOGOUT_URL);
	}
}
