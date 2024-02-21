package com.tf4.photospot.auth.infrastructure;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.tf4.photospot.auth.application.response.ApplePublicKeyResponse;

@HttpExchange("https://appleid.apple.com/auth")
public interface AppleClient {
	@GetExchange("/keys")
	ApplePublicKeyResponse getPublicKey();
}
