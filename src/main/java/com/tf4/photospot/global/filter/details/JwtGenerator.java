package com.tf4.photospot.global.filter.details;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.dto.LoginUserDto;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtGenerator {

	private static final String AUTHORITIES_DELIMITER = ",";

	private final JwtService jwtService;

	public void generate(HttpServletResponse response, LoginUserDto loginUser,
		Collection<? extends GrantedAuthority> authorities) {
		String accessToken = jwtService.issueAccessToken(loginUser.getId(),
			convertAuthoritiesToString(authorities));
		String refreshToken = jwtService.issueRefreshToken(loginUser.getId());

		response.setHeader(JwtConstant.AUTHORIZATION_HEADER, accessToken);
		response.addCookie(createCookie(refreshToken));
	}

	private String convertAuthoritiesToString(Collection<? extends GrantedAuthority> authorities) {
		Set<String> authoritiesSet = new HashSet<>();
		for (GrantedAuthority authority : authorities) {
			authoritiesSet.add(authority.getAuthority());
		}
		return String.join(AUTHORITIES_DELIMITER, authoritiesSet);
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
