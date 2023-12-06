package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.auth.application.response.ReissueTokenResponse;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.UserLoginResponse;
import com.tf4.photospot.user.domain.User;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;

	@Transactional
	public LoginTokenResponse login(String providerType, String account) {
		UserLoginResponse loginUser = userService.oauthLogin(providerType, account);
		return jwtService.issueTokens(loginUser.hasLoggedInBefore(),
			userService.findUser(providerType, loginUser.getAccount()));
	}

	public ReissueTokenResponse reissueToken(String refreshToken) {
		Claims claims = jwtService.parse(refreshToken);
		User user = userService.findUser(claims.get("id", Long.class));
		jwtService.validRefreshToken(user.getId(), refreshToken);
		return new ReissueTokenResponse(jwtService.reissueAccessToken(user));
	}
}
