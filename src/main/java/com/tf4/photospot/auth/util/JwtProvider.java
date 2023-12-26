package com.tf4.photospot.auth.util;

import static com.tf4.photospot.global.config.jwt.JwtConstant.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.tf4.photospot.global.config.jwt.JwtConstant;
import com.tf4.photospot.global.config.jwt.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtProvider {

	private final JwtProperties jwtProperties;

	public String generateAccessToken(Long userId, String authorities) {
		Date now = new Date();
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
			.claim(USER_ID, userId)
			.claim(USER_AUTHORITIES, authorities)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + JwtConstant.ACCESS_TOKEN_DURATION.toMillis()))
			.signWith(key).compact();
	}

	public String generateRefreshToken(Long userId) {
		Date now = new Date();
		SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
			.claim(USER_ID, userId)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + JwtConstant.REFRESH_TOKEN_DURATION.toMillis()))
			.signWith(key).compact();
	}

}
