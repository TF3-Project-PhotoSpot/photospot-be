package com.tf4.photospot.auth.infrastructure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.service.annotation.HttpExchange;

import com.tf4.photospot.auth.application.response.ApplePublicKeyResponse;

@HttpExchange("https://appleid.apple.com/auth")
public interface AppleClient {

	@GetMapping("/keys")
	ApplePublicKeyResponse getApplePublicKey();
}
