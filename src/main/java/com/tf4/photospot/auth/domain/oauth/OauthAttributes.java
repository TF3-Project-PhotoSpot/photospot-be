package com.tf4.photospot.auth.domain.oauth;

import java.util.Arrays;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthAttributes {

	KAKAO("kakao") {
		@Override
		public OauthUserInfo of(Map<String, Object> attributes) {
			return new OauthUserInfo(
				String.valueOf(attributes.get("id")));
		}
	};

	public final String type;

	public abstract OauthUserInfo of(Map<String, Object> attributes);

	// Todo : 예외 처리
	public static OauthAttributes findByProviderType(String type) {
		return Arrays.stream(OauthAttributes.values())
			.filter(provider -> type.equals(provider.type))
			.findFirst()
			.orElseThrow();
	}

}
