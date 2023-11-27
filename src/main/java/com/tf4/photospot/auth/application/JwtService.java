package com.tf4.photospot.auth.application;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRepository;
import com.tf4.photospot.auth.util.JwtProvider;
import com.tf4.photospot.config.jwt.JwtProperties;
import com.tf4.photospot.user.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class JwtService {

	private static final String PREFIX = "Bearer ";
	private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
	private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
	private final JwtProvider jwtProvider;
	private final JwtRepository jwtRepository;
	private final JwtProperties jwtProperties;

	@Transactional
	public LoginTokenResponse issueTokens(boolean hasLoggedInBefore, User user) {
		String accessToken = jwtProvider.generateToken(user, ACCESS_TOKEN_DURATION);
		String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_DURATION);

		jwtRepository.save(new RefreshToken(user.getId(), refreshToken));
		return new LoginTokenResponse(hasLoggedInBefore, accessToken, refreshToken);
	}

	public String reissueAccessToken(User user) {
		return jwtProvider.generateToken(user, ACCESS_TOKEN_DURATION);
	}

	// Todo : 예외 처리
	public Claims parse(String authorizationHeader) {
		String token = removePrefix(authorizationHeader);
		try {
			return Jwts.parser()
				.setSigningKey(jwtProperties.getSecretKey())
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException ex) {
			throw new RuntimeException();
		} catch (UnsupportedJwtException | MalformedJwtException | SignatureException ex) {
			throw new RuntimeException();
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException();
		}
	}

	// Todo : 예외 처리
	public void validRefreshToken(Long userId, String refreshToken) {
		RefreshToken token = jwtRepository.findByUserId(userId)
			.orElseThrow();

		if (!token.isTokenMatching(removePrefix(refreshToken))) {
			throw new RuntimeException();
		}
	}

	// Todo : 예외 처리
	private String removePrefix(String header) {
		if (header == null || !header.startsWith(PREFIX)) {
			throw new RuntimeException();
		}
		return header.substring(PREFIX.length());
	}

}
