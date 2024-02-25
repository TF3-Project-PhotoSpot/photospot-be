package com.tf4.photospot.global.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AuthService authService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		Authentication auth;
		if (request.getRequestURI().equals(SecurityConstant.REISSUE_ACCESS_TOKEN_URL)) {
			String refreshToken = extractRefreshTokenFromHeader(request);
			Claims claims = jwtService.parseRefreshToken(refreshToken);
			Long userId = claims.get(JwtConstant.USER_ID, Long.class);
			auth = new UsernamePasswordAuthenticationToken(new LoginUserDto(userId), null, null);
		} else {
			String jwt = request.getHeader(JwtConstant.AUTHORIZATION_HEADER);
			validate(jwt);
			Claims claims = jwtService.parseAccessToken(jwt);
			Long userId = claims.get(JwtConstant.USER_ID, Long.class);
			String authorities = String.valueOf(claims.get(JwtConstant.USER_AUTHORITIES));
			auth = new UsernamePasswordAuthenticationToken(new LoginUserDto(userId), null,
				AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
		}
		SecurityContextHolder.getContext().setAuthentication(auth);
		filterChain.doFilter(request, response);
	}

	private void validate(String accessToken) {
		if (accessToken == null || authService.existsBlacklist(accessToken)) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
	}

	private String extractRefreshTokenFromHeader(HttpServletRequest request) {
		String refreshToken = request.getHeader(JwtConstant.AUTHORIZATION_HEADER);
		if (!StringUtils.hasText(refreshToken)) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
		return refreshToken;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getRequestURI().equals(SecurityConstant.LOGIN_URL);
	}
}
