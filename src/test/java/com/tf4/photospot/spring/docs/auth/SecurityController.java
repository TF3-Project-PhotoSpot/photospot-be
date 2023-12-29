package com.tf4.photospot.spring.docs.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.auth.application.response.LoginResponse;
import com.tf4.photospot.global.dto.ApiResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/api/v1/auth")
@RestController
public class SecurityController {

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(HttpServletResponse response) {
		Cookie cookie = new Cookie("RefreshToken", "refresh token value");
		response.addCookie(cookie);
		return ApiResponse.success(new LoginResponse("access token", false));
	}
}
