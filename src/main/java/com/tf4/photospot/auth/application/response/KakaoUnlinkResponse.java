package com.tf4.photospot.auth.application.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KakaoUnlinkResponse {
	@JsonProperty("id")
	private Long account;
}
