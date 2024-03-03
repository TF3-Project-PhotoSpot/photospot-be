package com.tf4.photospot.auth.presentation.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUnlinkCallbackInfo(
	@JsonProperty("app_id")
	String appId,

	@JsonProperty("user_id")
	String account,

	@JsonProperty("referer_type")
	String refererType
) {
}
