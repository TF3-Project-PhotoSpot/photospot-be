package com.tf4.photospot.user.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceTest extends IntegrationTestSupport {
	private final UserService userService;
	private final UserRepository userRepository;

	@Test
	@DisplayName("사용자의 프로필 사진을 업데이트 한다.")
	void updateProfile() {
		// given
		var user = createUser("nickname", "kakao", "account_value");
		var userId = userRepository.save(user).getId();
		var imageUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/example.webp";

		// when
		userService.updateProfile(userId, imageUrl);

		// then
		assertThat(userRepository.findById(userId).orElseThrow().getProfileUrl()).isEqualTo(imageUrl);
	}
}
