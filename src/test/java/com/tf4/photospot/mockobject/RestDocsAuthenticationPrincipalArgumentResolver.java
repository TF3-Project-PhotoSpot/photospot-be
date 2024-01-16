package com.tf4.photospot.mockobject;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.LoginUserDto;

import jakarta.validation.constraints.NotNull;

public class RestDocsAuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
			|| parameter.hasParameterAnnotation(AuthUserId.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		@NotNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		if (parameter.hasParameterAnnotation(AuthUserId.class)) {
			return 1L;
		}
		return new LoginUserDto(1L);
	}
}
