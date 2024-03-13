package com.tf4.photospot.auth.presentation.request;

public record KakaoUnlinkCallbackInfo(
	String appId,
	String account,
	String refererType
) {
}
