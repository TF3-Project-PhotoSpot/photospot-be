package com.tf4.photospot.user.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
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
		var user = createUser("nickname", "123456", "kakao");
		var userId = userRepository.save(user).getId();
		var imageUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/example.webp";

		// when
		userService.updateProfile(userId, imageUrl);

		// then
		assertThat(userRepository.findById(userId).orElseThrow().getProfileUrl()).isEqualTo(imageUrl);
	}

	@TestFactory
	Stream<DynamicTest> updateNickname() {
		var user = createUser("original", "123456", "kakao");
		var otherUser = createUser("사용자", "456789", "apple");
		var userId = userRepository.save(user).getId();
		userRepository.save(otherUser);
		return Stream.of(
			dynamicTest("유효한 닉네임으로 변경 성공한다.", () -> {
				String nickname = "renewal";
				userService.updateNickname(userId, nickname);
				assertThat(user.getNickname()).isEqualTo("renewal");
			}),
			dynamicTest("중복 닉네임으로 변경 시 예외를 던진다.", () -> {
				String nickname = "사용자";
				assertThatThrownBy(() -> userService.updateNickname(userId, nickname))
					.isInstanceOf(ApiException.class).hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());
			})
		);
	}

	@TestFactory
	Stream<DynamicTest> getUserInfo() {
		var user = createUser("사용자");
		user.updateProfile("image.com");
		Long userId = userRepository.save(user).getId();
		return Stream.of(
			dynamicTest("사용자 정보 조회를 성공한다.", () -> {
				var response = userService.getInfo(userId);
				assertThat(response.userId()).isEqualTo(userId);
				assertThat(response.nickname()).isEqualTo("사용자");
				assertThat(response.profileUrl()).isEqualTo("image.com");
			}),
			dynamicTest("존재하지 않는 사용자 정보 조회 시 예외를 던진다.", () -> {
				assertThatThrownBy(() -> userService.getInfo(100L))
					.isInstanceOf(ApiException.class).hasMessage(UserErrorCode.NOT_FOUND_USER.getMessage());
			})
		);
	}
}
