package com.tf4.photospot.global.filter.details;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.auth.application.response.LoginResponse;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.LoginUserDto;

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
			convertAuthoritiesToString(authentication.getAuthorities()));
		String refreshToken = jwtService.issueRefreshToken(loginUser.getId());

		response.addCookie(createCookie(refreshToken));
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		new ObjectMapper().writeValue(response.getWriter(), createBody(accessToken, loginUser.hasLoggedInBefore()));
	}

	// Todo : 예외 변경
	private LoginUserDto getOauthUserFromAuthentication(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof LoginUserDto) {
			return (LoginUserDto)authentication.getPrincipal();
		}
		throw new RuntimeException("invalid authentication");
	}

	private String convertAuthoritiesToString(Collection<? extends GrantedAuthority> authorities) {
		Set<String> authoritiesSet = new HashSet<>();
		for (GrantedAuthority authority : authorities) {
			authoritiesSet.add(authority.getAuthority());
		}
		return String.join(",", authoritiesSet);
	}

	private ApiResponse<LoginResponse> createBody(String accessToken, Boolean hasLoggedInBefore) {
		return ApiResponse.success(new LoginResponse(accessToken, hasLoggedInBefore));
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
