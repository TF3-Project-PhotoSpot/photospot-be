package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.response.NicknameUpdateResponse;
import com.tf4.photospot.user.application.response.ProfileUpdateResponse;
import com.tf4.photospot.user.application.response.UserInfoResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;
import com.tf4.photospot.user.infrastructure.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserQueryRepository userQueryRepository;
	private final UserRepository userRepository;

	@Transactional
	public ProfileUpdateResponse updateProfile(Long userId, String imageUrl) {
		User user = getActiveUser(userId);
		user.updateProfile(imageUrl);
		return new ProfileUpdateResponse(imageUrl);
	}

	@Transactional
	public NicknameUpdateResponse updateNickname(Long userId, String nickname) {
		if (isNicknameDuplicated(nickname)) {
			throw new ApiException(UserErrorCode.DUPLICATE_NICKNAME);
		}
		User user = getActiveUser(userId);
		user.updateNickname(nickname);
		return new NicknameUpdateResponse(nickname);
	}

	public UserInfoResponse getInfo(Long userId) {
		return UserInfoResponse.of(getActiveUser(userId));
	}

	public User getActiveUser(Long userId) {
		return userQueryRepository.findActiveUserById(userId)
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
	}

	public boolean isNicknameDuplicated(String nickname) {
		return userRepository.existsByNickname(nickname);
	}
}
