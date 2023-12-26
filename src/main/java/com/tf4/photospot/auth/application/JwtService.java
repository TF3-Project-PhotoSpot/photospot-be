package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.domain.JwtRepository;
import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.util.JwtProvider;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.config.jwt.JwtProperties;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class JwtService {
	private final JwtProvider jwtProvider;
	private final JwtRepository jwtRepository;
	private final JwtProperties jwtProperties;

	public String issueAccessToken(Long userId, String authorities) {
		return jwtProvider.generateAccessToken(userId, authorities);
	}

	public String issueRefreshToken(Long userId) {
		return jwtProvider.generateRefreshToken(userId);
	}

	public Claims parse(String authorizationHeader) {
		String token = removePrefix(authorizationHeader);
		try {
			return Jwts.parserBuilder()
				.setSigningKey(jwtProperties.getSecretKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException ex) {
			throw new ApiException(AuthErrorCode.EXPIRED_TOKEN);
		} catch (UnsupportedJwtException | MalformedJwtException | SecurityException ex) {
			throw new ApiException(AuthErrorCode.INVALID_TOKEN);
		}
	}

	public void validRefreshToken(Long userId, String refreshToken) {
		RefreshToken token = jwtRepository.findByUserId(userId)
			.orElseThrow(() -> new ApiException(AuthErrorCode.EXPIRED_TOKEN));

		if (!token.isTokenMatching(removePrefix(refreshToken))) {
			throw new ApiException(AuthErrorCode.INVALID_TOKEN);
		}
	}

	private String removePrefix(String header) {
		if (header == null || !header.startsWith(JwtConstant.PREFIX)) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
		return header.substring(JwtConstant.PREFIX.length());
	}

	@Transactional
	public void removeRefreshToken(Long userId) {
		jwtRepository.deleteById(userId);
	}

}
