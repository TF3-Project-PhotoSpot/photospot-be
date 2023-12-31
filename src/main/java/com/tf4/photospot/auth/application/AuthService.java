package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;

	public ReissueTokenResponse reissueToken(Long userId, String refreshToken) {
		jwtService.validRefreshToken(userId, refreshToken);
		User user = userService.findUser(userId);
		return new ReissueTokenResponse(jwtService.issueAccessToken(user.getId(), user.getRole().type));
	}
}
