package com.tf4.photospot.user.presentation;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.domain.oauth.OauthUserInfo;
import com.tf4.photospot.auth.presentation.response.UserLoginResponse;
import com.tf4.photospot.auth.util.NicknameGenerator;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public UserLoginResponse oauthLogin(String providerName, OauthUserInfo userInfo) {
		User user = userInfo.toUser(providerName, generateNickname());
		Optional<User> findUser = userRepository.findUserByAccountAndProviderType(user.getAccount(),
			user.getProviderType());
		return loginOrSignup(user, findUser);
	}

	private UserLoginResponse loginOrSignup(User user, Optional<User> findUser) {
		if (findUser.isEmpty()) {
			return new UserLoginResponse(false, userRepository.save(user));
		}
		return new UserLoginResponse(true, findUser.get());
	}

	private boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	private String generateNickname() {
		String generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		while (isNicknameDuplicated(generatedRandomNickname)) {
			generatedRandomNickname = NicknameGenerator.generatorRandomNickname();
		}
		return generatedRandomNickname;
	}
}
