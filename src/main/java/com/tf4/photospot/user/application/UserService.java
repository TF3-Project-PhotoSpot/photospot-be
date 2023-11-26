package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.auth.application.response.UserLoginResponse;
import com.tf4.photospot.auth.domain.oauth.OauthUserInfo;
import com.tf4.photospot.auth.util.NicknameGenerator;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public UserLoginResponse oauthLogin(String providerType, OauthUserInfo userInfo) {
		User user = userInfo.toUser(providerType, generateNickname());
		User findUser = findUser(userInfo.account(), providerType);
		return loginOrSignup(user, findUser);
	}

	public User findUser(String account, String providerType) {
		return userRepository.findUserByAccountAndProviderType(account, providerType).orElse(null);
	}

	private UserLoginResponse loginOrSignup(User user, User findUser) {
		if (findUser == null) {
			return UserLoginResponse.from(false, userRepository.save(user));
		}
		return UserLoginResponse.from(true, findUser);
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
