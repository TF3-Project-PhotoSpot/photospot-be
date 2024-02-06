package com.tf4.photospot.global.filter.details;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.tf4.photospot.auth.application.AuthService;
import com.tf4.photospot.global.dto.LoginUserDto;
import com.tf4.photospot.global.util.AuthorityConverter;
import com.tf4.photospot.user.application.response.OauthLoginUserResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private final AuthService authService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Map<String, String> identityInfo = (Map<String, String>)authentication.getPrincipal();
		String providerType = authentication.getCredentials().toString();
		OauthLoginUserResponse loginUser = authService.oauthLogin(providerType, identityInfo);
		List<GrantedAuthority> authorities = AuthorityConverter.convertStringToGrantedAuthority(
			loginUser.getRole().getType());
		return new UsernamePasswordAuthenticationToken(
			new LoginUserDto(loginUser.getId(), loginUser.hasLoggedInBefore()), null, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CustomAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
