package com.tf4.photospot.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

@Import(MockS3Config.class)
@Transactional
public class UserServiceTest extends IntegrationTestSupport {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MockS3Config mockS3Config;

	@DisplayName("사용자 등록 시나리오")
	@TestFactory
	Collection<DynamicTest> saveUser() {
		// given
		String providerType = "kakao";
		String account = "account";

		return List.of(
			DynamicTest.dynamicTest("최초 로그인 시 사용자 정보를 DB에 저장한다.", () -> {
				//when
				var loginResponse = userService.oauthLogin(providerType, account);

				//then
				assertAll(
					() -> assertThat(loginResponse.hasLoggedInBefore()).isFalse(),
					() -> assertThat(userRepository.findAll()).hasSize(1)
				);
			}),
			DynamicTest.dynamicTest("기존에 로그인 했던 사용자는 DB에 저장하지 않는다.", () -> {
				//when
				var loginResponse2 = userService.oauthLogin(providerType, account);
				User savedUser = userRepository.findUserByProviderTypeAndAccount(providerType, account).orElseThrow();

				//then
				assertAll(
					() -> assertThat(loginResponse2.hasLoggedInBefore()).isTrue(),
					() -> assertThat(userRepository.findAll()).hasSize(1),
					() -> assertThat(savedUser.getAccount()).isEqualTo(account)
				);
			})
		);
	}

	@Test
	@DisplayName("사용자의 프로필 사진을 업데이트 한다.")
	void updateProfile() {
		// given
		var user = new User("nickname", "kakao", "account_value");
		var userId = userRepository.save(user).getId();
		var file = new MockMultipartFile("file", "profile.jpeg", "image/jpeg", "<<jpeg data>>".getBytes());
		var request = "profile";

		// when
		var response = userService.updateProfile(userId, file, request);

		// then
		assertAll(
			() -> assertThat(response.imageUrl()).isEqualTo(mockS3Config.getDummyUrl()),
			() -> assertThat(userRepository.findById(userId).get().getProfileUrl()).isEqualTo(
				mockS3Config.getDummyUrl())
		);
	}
}
