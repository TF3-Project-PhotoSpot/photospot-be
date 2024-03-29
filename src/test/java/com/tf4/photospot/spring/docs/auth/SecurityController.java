package com.tf4.photospot.spring.docs.auth;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.response.LoginResponse;
import com.tf4.photospot.global.dto.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/api/v1/auth")
@RestController
public class SecurityController {

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
		return new LoginResponse("access_token", "refresh_token", false);
	}

	@DeleteMapping("/logout")
	public ApiResponse logout(@RequestHeader("Authorization") String accessToken, HttpServletResponse response) {
		return ApiResponse.SUCCESS;
	}
}

record LoginRequest(
	String providerType,
	String identifier,
	String token) {
}
