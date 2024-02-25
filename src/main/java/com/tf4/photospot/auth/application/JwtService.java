package com.tf4.photospot.auth.application;

import static com.tf4.photospot.global.config.jwt.JwtConstant.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.domain.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRedisRepository;
import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.config.jwt.JwtProperties;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

@Service
@Transactional(readOnly = true)
public class JwtService {
	private final JwtRedisRepository jwtRedisRepository;
	private final JwtProperties jwtProperties;

	private final SecretKey key;

	public JwtService(JwtRedisRepository jwtRedisRepository, JwtProperties jwtProperties) {
		this.jwtRedisRepository = jwtRedisRepository;
		this.jwtProperties = jwtProperties;
		this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
	}

	public String issueAccessToken(Long userId, String authorities) {
		return generateAccessToken(userId, authorities, new Date());
	}

	private String generateAccessToken(Long userId, String authorities, Date expiration) {
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
			.claim(USER_ID, userId)
			.claim(USER_AUTHORITIES, authorities)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(expiration)
			.setExpiration(new Date(expiration.getTime() + JwtConstant.ACCESS_TOKEN_DURATION.toMillis()))
			.signWith(key).compact();
	}

	@Transactional
	public String issueRefreshToken(Long userId) {
		String refreshToken = generateRefreshToken(userId, new Date());
		jwtRedisRepository.save(new RefreshToken(userId, refreshToken));
		return refreshToken;
	}

	private String generateRefreshToken(Long userId, Date expiration) {
		return Jwts.builder()
			.claim(USER_ID, userId)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(expiration)
			.setExpiration(new Date(expiration.getTime() + JwtConstant.REFRESH_TOKEN_DURATION.toMillis()))
			.signWith(key).compact();
	}

	public Claims parseAccessToken(String authorizationHeader) {
		String token = removePrefix(authorizationHeader);
		return parseToken(token, true);
	}

	public Claims parseRefreshToken(String token) {
		if (token == null) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
		return parseToken(token, false);
	}

	private Claims parseToken(String token, boolean isAccessToken) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(jwtProperties.getSecretKey().getBytes())
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (ExpiredJwtException ex) {
			throw new ApiException(
				isAccessToken ? AuthErrorCode.EXPIRED_ACCESS_TOKEN : AuthErrorCode.EXPIRED_REFRESH_TOKEN);
		} catch (UnsupportedJwtException | MalformedJwtException | SecurityException ex) {
			throw new ApiException(
				isAccessToken ? AuthErrorCode.INVALID_ACCESS_TOKEN : AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

	public void validRefreshToken(Long userId, String refreshToken) {
		RefreshToken token = jwtRedisRepository.findByUserId(userId)
			.orElseThrow(() -> new ApiException(AuthErrorCode.UNAUTHORIZED_USER));

		if (!token.isTokenMatching(refreshToken)) {
			throw new ApiException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

	private String removePrefix(String header) {
		if (header == null || !header.startsWith(JwtConstant.PREFIX)) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
		return header.substring(JwtConstant.PREFIX.length());
	}
}
