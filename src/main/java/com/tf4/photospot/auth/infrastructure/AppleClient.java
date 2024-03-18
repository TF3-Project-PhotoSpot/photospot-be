package com.tf4.photospot.auth.infrastructure;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.tf4.photospot.auth.application.request.AppleRevokeRequest;
import com.tf4.photospot.auth.application.request.GetAppleRefreshTokenRequest;
import com.tf4.photospot.auth.application.response.ApplePublicKeyResponse;
import com.tf4.photospot.auth.application.response.AppleTokenResponse;

@HttpExchange("https://appleid.apple.com/auth")
public interface AppleClient {
	@GetExchange("/keys")
	ApplePublicKeyResponse getPublicKey();

	@PostExchange(value = "/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	AppleTokenResponse generateToken(@RequestBody GetAppleRefreshTokenRequest request);

	@PostExchange(value = "/revoke", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	void revoke(@RequestBody AppleRevokeRequest request);
}
