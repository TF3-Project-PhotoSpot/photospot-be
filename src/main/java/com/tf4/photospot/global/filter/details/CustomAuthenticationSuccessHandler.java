package com.tf4.photospot.global.filter.details;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.auth.application.response.LoginResponse;
import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.util.AuthorityConverter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtService jwtService;

	private static final String CONTENT_TYPE = "application/json";
	private static final String CHARACTER_ENCODING = "UTF-8";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		LoginUserDto loginUser = getOauthUserFromAuthentication(authentication);
		String accessToken = jwtService.issueAccessToken(loginUser.getId(),
			AuthorityConverter.convertGrantedAuthoritiesToString(authentication.getAuthorities()));
		String refreshToken = jwtService.issueRefreshToken(loginUser.getId());

		response.setContentType(CONTENT_TYPE);
		response.setCharacterEncoding(CHARACTER_ENCODING);
		new ObjectMapper().writeValue(response.getWriter(),
			createBody(accessToken, refreshToken, loginUser.hasLoggedInBefore()));
	}

	private LoginUserDto getOauthUserFromAuthentication(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof LoginUserDto) {
			return (LoginUserDto)authentication.getPrincipal();
		}
		throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
	}

	private LoginResponse createBody(String accessToken, String refreshToken, Boolean hasLoggedInBefore) {
		return new LoginResponse(accessToken, refreshToken, hasLoggedInBefore);
	}
}
