package com.tf4.photospot.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.LoginTokenResponse;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.UserLoginResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;

	// Todo : 예외 수정
	@Transactional
	public LoginTokenResponse login(String providerType, String account) {
		UserLoginResponse loginUser = userService.oauthLogin(providerType, account);
		return jwtService.issueTokens(loginUser.hasLoggedInBefore(),
			userService.findUser(providerType, loginUser.getAccount()));
	}
}
