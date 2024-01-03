package com.tf4.photospot.global.filter.details;

import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.util.AuthorityConverter;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.application.response.OauthLoginUserResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private final UserService userService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String account = authentication.getPrincipal().toString();
		String providerType = authentication.getCredentials().toString();
		OauthLoginUserResponse loginUser = userService.oauthLogin(providerType, account);
		List<GrantedAuthority> authorities = AuthorityConverter.convertStringToGrantedAuthority(
			loginUser.getRole().type);
		return new UsernamePasswordAuthenticationToken(
			new LoginUserDto(loginUser.getId(), loginUser.hasLoggedInBefore()), null, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CustomAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
