package com.tf4.photospot.mockobject;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.util.AuthorityConverter;
import com.tf4.photospot.user.application.response.OauthLoginResponse;

public class WithCustomerMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

	@Override
	public SecurityContext createSecurityContext(WithCustomMockUser mockUser) {
		OauthLoginResponse loginUser = new OauthLoginResponse(mockUser.hasLoggedInBefore(), mockUser.userId(),
			mockUser.role());
		Authentication auth = new UsernamePasswordAuthenticationToken(
			new LoginUserDto(loginUser.getId(), loginUser.hasLoggedInBefore()), null,
			AuthorityConverter.convertStringToGrantedAuthority(loginUser.getRole().type));
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);
		return context;
	}
}
