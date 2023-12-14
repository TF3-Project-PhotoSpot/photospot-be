package com.tf4.photospot.auth.util;

import java.time.Duration;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.tf4.photospot.global.config.jwt.JwtProperties;
import com.tf4.photospot.user.domain.User;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtProvider {

	private final JwtProperties jwtProperties;

	public String generateToken(User user, Duration expiredAt) {
		Date now = new Date();
		return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
	}

	private String makeToken(Date expiry, User user) {
		Date now = new Date();

		return Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setIssuer(jwtProperties.getIssuer())
			.setIssuedAt(now)
			.setExpiration(expiry)
			.claim("id", user.getId())
			.signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
			.compact();
	}

}
