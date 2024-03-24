package com.tf4.photospot.auth.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.auth.util.KeyParser;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;

public class KeyParserTest {

	@TestFactory
	Stream<DynamicTest> getPrivateKeyTest() {
		return Stream.of(
			dynamicTest("비어있는 key를 파싱하는 경우 예외를 던진다", () -> {
				String key = null;
				assertThatThrownBy(() -> KeyParser.getPrivateKey(key))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.EMPTY_PRIVATE_KEY.getMessage());
			}),
			dynamicTest("유효하지 않은 key를 파싱하는 경우 예외를 던진다", () -> {
				String key = "wrong_key";
				assertThatThrownBy(() -> KeyParser.getPrivateKey(key))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.INVALID_PRIVATE_KEY.getMessage());
			})
		);
	}
}
