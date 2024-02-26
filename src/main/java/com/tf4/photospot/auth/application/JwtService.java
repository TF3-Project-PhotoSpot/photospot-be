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
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

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
	private final UserRepository userRepository;

	private final SecretKey key;

	public JwtService(JwtRedisRepository jwtRedisRepository, JwtProperties jwtProperties,
		UserRepository userRepository) {
		this.jwtRedisRepository = jwtRedisRepository;
		this.jwtProperties = jwtProperties;
		this.userRepository = userRepository;
		this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
	}

	public String issueAccessToken(Long userId, String authorities) {
		return generateAccessToken(findUser(userId).getId(), authorities, new Date());
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

	public String issueRefreshToken(Long userId) {
		Long loginUserId = findUser(userId).getId();
		String token = generateRefreshToken(loginUserId, new Date());
		if (jwtRedisRepository.existsByUserId(loginUserId)) {
			jwtRedisRepository.deleteByUserId(loginUserId);
		}
		jwtRedisRepository.save(new RefreshToken(loginUserId, token));
		return token;
	}

	private String generateRefreshToken(Long userId, Date expiration) {
		return Jwts.builder()
			.claim(USER_ID, userId)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(expiration)
			.setExpiration(new Date(expiration.getTime() + JwtConstant.REFRESH_TOKEN_DURATION.toMillis()))
			.signWith(key).compact();
	}

	public Claims parseAccessToken(String token) {
		String accessToken = removePrefix(token);
		return parseToken(accessToken, true);
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

	private String removePrefix(String token) {
		if (token == null || !token.startsWith(JwtConstant.PREFIX)) {
			throw new ApiException(AuthErrorCode.UNAUTHORIZED_USER);
		}
		return token.substring(JwtConstant.PREFIX.length());
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new ApiException(AuthErrorCode.NOT_FOUND_USER));
	}
}
