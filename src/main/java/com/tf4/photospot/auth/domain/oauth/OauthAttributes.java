package com.tf4.photospot.auth.domain.oauth;

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
				String.valueOf(attributes.get("id")),
				String.valueOf(attributes.get("name")));
		}
	};

	public final String providerName;

	public abstract OauthUserInfo of(Map<String, Object> attributes);

}
