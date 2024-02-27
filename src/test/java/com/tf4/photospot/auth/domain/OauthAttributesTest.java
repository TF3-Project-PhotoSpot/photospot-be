package com.tf4.photospot.auth.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class OauthAttributesTest {

	@TestFactory
	Stream<DynamicTest> findByType() {
		// given
		String provider = "kakao";
		return Stream.of(
			DynamicTest.dynamicTest("유효한 공급자를 확인한다", () -> {
				// when & then
				assertThat(OauthAttributes.findByType(provider).getProvider()).isEqualTo(provider);
			})
		);
	}
}
