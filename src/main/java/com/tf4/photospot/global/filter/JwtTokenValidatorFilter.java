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

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	// Todo : 예외 처리
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String jwt = request.getHeader(JwtConstant.AUTHORIZATION_HEADER);
		if (jwt == null) {
			throw new RuntimeException("jwt is null");
		}

		// Todo : try-catch 문이 불필요해보이는데 보수적으로 넣을지 & 예외 처리
		try {
			Claims claims = jwtService.parse(jwt, JwtConstant.AUTHORIZATION_HEADER);
			Long userId = Long.valueOf(claims.getId());
			String authorities = String.valueOf(claims.get(JwtConstant.USER_AUTHORITIES));
			Authentication auth = new UsernamePasswordAuthenticationToken(new LoginUserDto(userId), null,
				AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception ex) {
			throw new RuntimeException("invalid jwt");
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().equals(SecurityConstant.LOGIN_URL) || request.getServletPath()
			.equals(SecurityConstant.REISSUE_ACCESS_TOKEN_URL);
	}
}
