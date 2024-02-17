package com.tf4.photospot.global.filter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.filter.details.CustomAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
		super.setAuthenticationManager(authenticationManager);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		String providerType = OauthAttributes.findByType(request.getParameter(SecurityConstant.PROVIDER_TYPE_PARAM))
			.getProvider();
		Map<String, String> identityInfo = new HashMap<>();
		if (providerType.equals(OauthAttributes.KAKAO.getProvider())) {
			identityInfo.put(SecurityConstant.ACCOUNT_PARAM, request.getParameter(SecurityConstant.ACCOUNT_PARAM));
		} else if (providerType.equals(OauthAttributes.APPLE.getProvider())) {
			identityInfo.put(SecurityConstant.IDENTITY_TOKEN_PARAM,
				request.getParameter(SecurityConstant.IDENTITY_TOKEN_PARAM));
			identityInfo.put(SecurityConstant.NONCE_PARAM, request.getParameter(SecurityConstant.NONCE_PARAM));
		}
		CustomAuthenticationToken authToken = new CustomAuthenticationToken(identityInfo, providerType);
		authToken.setDetails(this.authenticationDetailsSource.buildDetails(request));
		return this.getAuthenticationManager().authenticate(authToken);
	}
}
