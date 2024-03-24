package com.tf4.photospot.auth.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

public class OauthAttributesTest {

	@TestFactory
	Stream<DynamicTest> findByType() {
		// given
		String provider = "kakao";
		return Stream.of(
			dynamicTest("kakao 공급자를 확인한다", () -> {
				// when & then
				assertThat(OauthAttributes.findByType(provider).getProvider()).isEqualTo(provider);
			}),
			dynamicTest("apple 공급자를 확인한다", () -> {
				// given
				String provider2 = "apple";

				// when & then
				assertThat(OauthAttributes.findByType(provider2).getProvider()).isEqualTo(provider2);
			}),
			dynamicTest("유효하지 않은 공급자를 찾으면 예외를 던진다", () -> {
				// given
				String wrongProvider = "kekeo";

				// when & then
				assertThatThrownBy(() -> OauthAttributes.findByType(wrongProvider))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.INVALID_PROVIDER_TYPE.getMessage());
			})
		);
	}
}
