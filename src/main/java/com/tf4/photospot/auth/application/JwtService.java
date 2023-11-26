package com.tf4.photospot.auth.application;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.domain.jwt.RefreshToken;
import com.tf4.photospot.auth.infrastructure.JwtRepository;
import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.util.jwt.JwtProvider;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class JwtService {

	private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
	private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
	private final JwtProvider jwtProvider;
	private final JwtRepository jwtRepository;

	@Transactional
	public LoginTokenResponse issueTokens(boolean hasLoggedInBefore, User user) {
		String accessToken = jwtProvider.generateToken(user, ACCESS_TOKEN_DURATION);
		String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_DURATION);

		jwtRepository.save(new RefreshToken(user.getId(), refreshToken));
		return new LoginTokenResponse(hasLoggedInBefore, accessToken, refreshToken);
	}
}
