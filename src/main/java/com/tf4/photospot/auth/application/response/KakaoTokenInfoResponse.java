package com.tf4.photospot.auth.application.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KakaoTokenInfoResponse {
	private Long id;

	@JsonProperty("expires_in")
	private Integer expiresIn;

	@JsonProperty("app_id")
	private Integer appId;
}
