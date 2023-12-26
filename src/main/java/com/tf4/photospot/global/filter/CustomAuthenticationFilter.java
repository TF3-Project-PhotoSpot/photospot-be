package com.tf4.photospot.global.filter;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tf4.photospot.auth.domain.OauthAttributes;
import com.tf4.photospot.global.config.security.SecurityConstant;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
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
		String account = request.getParameter(SecurityConstant.ACCOUNT_PARAM);
		String providerType = request.getParameter(SecurityConstant.PROVIDER_TYPE_PARAM);

		try {
			OauthAttributes.findByType(providerType);
		} catch (IllegalArgumentException ex) {
			throw new ApiException(AuthErrorCode.INVALID_PROVIDER_TYPE);
		}

		CustomAuthenticationToken authToken = new CustomAuthenticationToken(account, providerType);
		authToken.setDetails(this.authenticationDetailsSource.buildDetails(request));
		return this.getAuthenticationManager().authenticate(authToken);
	}

}
