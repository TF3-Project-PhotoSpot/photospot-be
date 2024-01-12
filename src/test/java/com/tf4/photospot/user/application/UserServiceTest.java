package com.tf4.photospot.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

@Transactional
public class UserServiceTest extends IntegrationTestSupport {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

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
}
