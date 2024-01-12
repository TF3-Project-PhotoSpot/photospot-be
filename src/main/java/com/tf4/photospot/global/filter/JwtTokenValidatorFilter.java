package com.tf4.photospot.global.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

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

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String jwt = request.getHeader(JwtConstant.AUTHORIZATION_HEADER);
		validate(jwt);
		Claims claims = jwtService.parseAccessToken(jwt);
		Long userId = claims.get("id", Long.class);
		String authorities = String.valueOf(claims.get(JwtConstant.USER_AUTHORITIES));
		Authentication auth = new UsernamePasswordAuthenticationToken(new LoginUserDto(userId), null,
			AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
		SecurityContextHolder.getContext().setAuthentication(auth);

		filterChain.doFilter(request, response);
	}

	private void validate(String accessToken) {
		if (accessToken == null) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getRequestURI().equals(SecurityConstant.LOGIN_URL) || request.getRequestURI()
			.equals(SecurityConstant.REISSUE_ACCESS_TOKEN_URL);
	}
}
