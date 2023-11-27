package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.user.application.request.LoginUserInfo;
import com.tf4.photospot.user.application.response.UserLoginResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserRepository;
import com.tf4.photospot.user.util.NicknameGenerator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public UserLoginResponse oauthLogin(String providerType, String account) {
		return userRepository.findUserByProviderTypeAndAccount(providerType, account)
			.map(findUser -> UserLoginResponse.from(true, findUser))
			.orElseGet(() -> UserLoginResponse.from(false, userRepository.save(
				new LoginUserInfo(providerType, account).toUser(generateNickname())
			)));
	}

	// Todo : 예외 처리
	public User findUser(String providerType, String account) {
		return userRepository.findUserByProviderTypeAndAccount(providerType, account)
			.orElseThrow();
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
