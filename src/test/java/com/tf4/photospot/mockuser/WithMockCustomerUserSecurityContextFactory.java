package com.tf4.photospot.mockuser;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.util.AuthorityConverter;
import com.tf4.photospot.user.application.response.OauthLoginUserResponse;

public class WithMockCustomerUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser mockUser) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		OauthLoginUserResponse loginUser = new OauthLoginUserResponse(mockUser.hasLoggedInBefore(), mockUser.id(),
			mockUser.role());
		Authentication auth = new UsernamePasswordAuthenticationToken(
			new LoginUserDto(loginUser.getId(), loginUser.hasLoggedInBefore()), null,
			AuthorityConverter.convertStringToGrantedAuthority(loginUser.getRole()));
		context.setAuthentication(auth);
		return context;
	}
}
