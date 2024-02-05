package com.tf4.photospot.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.user.application.response.UserProfileResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public UserProfileResponse updateProfile(Long userId, String imageUrl) {
		// 필요한 부분?
		User loginUser = userRepository.findById(userId)
			.orElseThrow(() -> new ApiException(AuthErrorCode.NOT_FOUND_USER));
		loginUser.updateProfile(imageUrl);
		return new UserProfileResponse(imageUrl);
	}
}
