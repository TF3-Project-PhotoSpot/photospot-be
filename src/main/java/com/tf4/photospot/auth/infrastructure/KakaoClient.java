package com.tf4.photospot.auth.infrastructure;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.tf4.photospot.auth.application.response.KakaoTokenInfoResponse;
import com.tf4.photospot.global.config.jwt.JwtConstant;

@HttpExchange("https://kapi.kakao.com/v1/user")
public interface KakaoClient {
	@GetExchange("/access_token_info")
	KakaoTokenInfoResponse getTokenInfo(@RequestHeader(JwtConstant.AUTHORIZATION_HEADER) String authorization);
}
