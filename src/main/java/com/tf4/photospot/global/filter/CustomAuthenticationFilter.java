package com.tf4.photospot.global.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.auth.presentation.request.LoginDto;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.filter.details.CustomAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final ObjectMapper objectMapper = new ObjectMapper();

	public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
		super.setAuthenticationManager(authenticationManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		try {
			LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);
			String providerType = OauthAttributes.findByType(loginDto.getProviderType()).getProvider();
			Map<String, String> identifyInfo = new HashMap<>();
			identifyInfo.put(SecurityConstant.TOKEN, loginDto.getToken());
			identifyInfo.put(SecurityConstant.IDENTIFIER, loginDto.getIdentifier());
			CustomAuthenticationToken authToken = new CustomAuthenticationToken(identifyInfo, providerType);
			authToken.setDetails(this.authenticationDetailsSource.buildDetails(request));
			return this.getAuthenticationManager().authenticate(authToken);
		} catch (IOException ex) {
			throw new ApiException(AuthErrorCode.INVALID_LOGIN_REQUEST);
		}
	}
}
