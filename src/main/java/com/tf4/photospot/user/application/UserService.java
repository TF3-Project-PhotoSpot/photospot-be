package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.user.application.response.UserProfileResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserQueryRepository userQueryRepository;

	@Transactional
	public UserProfileResponse updateProfile(Long userId, String imageUrl) {
		User loginUser = getActiveUser(userId);
		loginUser.updateProfile(imageUrl);
		return new UserProfileResponse(imageUrl);
	}

	public User getActiveUser(Long userId) {
		return userQueryRepository.findActiveUserById(userId)
			.orElseThrow(() -> new ApiException(UserErrorCode.NOT_FOUND_USER));
	}
}
