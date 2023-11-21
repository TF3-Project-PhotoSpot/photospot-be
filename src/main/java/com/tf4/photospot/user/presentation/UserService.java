package com.tf4.photospot.user.presentation;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tf4.photospot.auth.domain.oauth.OauthUserInfo;
import com.tf4.photospot.auth.presentation.response.UserLoginResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	public UserLoginResponse oauthLogin(String providerName, OauthUserInfo userInfo) {
		User user = userInfo.toUser(providerName);
		Optional<User> findUser = userRepository.findUserByAccountAndProviderType(user.getAccount(),
			user.getProviderType());
		return loginOrSignup(user, findUser);
	}

	private UserLoginResponse loginOrSignup(User user, Optional<User> findUser) {
		return findUser.map(existingUser -> new UserLoginResponse(true, existingUser))
			.orElse(new UserLoginResponse(false, userRepository.save(user)));
	}

}
