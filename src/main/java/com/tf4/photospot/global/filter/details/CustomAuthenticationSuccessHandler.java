package com.tf4.photospot.global.filter.details;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.auth.application.response.LoginResponse;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.util.AuthorityConverter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final JwtService jwtService;

	// Todo : 상수 처리
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		LoginUserDto loginUser = getOauthUserFromAuthentication(authentication);
		String accessToken = jwtService.issueAccessToken(loginUser.getId(),
			AuthorityConverter.convertGrantedAuthoritiesToString(authentication.getAuthorities()));
		String refreshToken = jwtService.issueRefreshToken(loginUser.getId());

		response.addCookie(createCookie(refreshToken));
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		new ObjectMapper().writeValue(response.getWriter(), createBody(accessToken, loginUser.hasLoggedInBefore()));
	}

	private LoginUserDto getOauthUserFromAuthentication(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof LoginUserDto) {
			return (LoginUserDto)authentication.getPrincipal();
		}
		throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
	}

	private LoginResponse createBody(String accessToken, Boolean hasLoggedInBefore) {
		return new LoginResponse(accessToken, hasLoggedInBefore);
	}

	// Todo : https 연결 후 setSecure(true)로 변경하기
	private Cookie createCookie(String refreshToken) {
		Cookie cookie = new Cookie(JwtConstant.REFRESH_COOKIE_NAME, refreshToken);
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge((int)JwtConstant.REFRESH_TOKEN_DURATION.toSeconds());
		return cookie;
	}
}
