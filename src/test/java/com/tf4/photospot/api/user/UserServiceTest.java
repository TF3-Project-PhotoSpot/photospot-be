package com.tf4.photospot.api.user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.auth.domain.oauth.OauthUserInfo;
import com.tf4.photospot.auth.presentation.response.UserLoginResponse;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.infrastructure.UserRepository;
import com.tf4.photospot.user.presentation.UserService;

public class UserServiceTest extends IntegrationTestSupport {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@DisplayName("사용자 등록 시나리오")
	@TestFactory
	Collection<DynamicTest> saveUser() {
		// given
		String providerName = "kakao";
		OauthUserInfo userInfo = new OauthUserInfo("account");

		return List.of(
			DynamicTest.dynamicTest("최초 로그인 시 사용자 정보를 DB에 저장한다.", () -> {
				//when
				UserLoginResponse loginResponse = userService.oauthLogin(providerName, userInfo);

				//then
				assertAll(
					() -> assertThat(loginResponse.hasLoggedInBefore()).isFalse(),
					() -> assertThat(userRepository.findAll()).hasSize(1)
				);
			}),
			DynamicTest.dynamicTest("기존에 로그인 했던 사용자는 DB에 저장하지 않는다.", () -> {
				//when
				UserLoginResponse loginResponse2 = userService.oauthLogin(providerName, userInfo);
				User savedUser = userRepository.findById(1L).orElseThrow();

				//then
				assertAll(
					() -> assertThat(loginResponse2.hasLoggedInBefore()).isTrue(),
					() -> assertThat(userRepository.findAll()).hasSize(1),
					() -> assertThat(savedUser.getAccount()).isEqualTo(userInfo.getAccount())
				);
			})
		);
	}
}
