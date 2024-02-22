package com.tf4.photospot.spring.docs.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.response.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/api/v1/auth")
@RestController
public class SecurityController {

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
		return new LoginResponse("access_token", "refresh_token", false);
	}
}

record LoginRequest(
	String providerType,
	String identifier,
	String token) {
}
