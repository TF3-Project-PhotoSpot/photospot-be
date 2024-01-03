package com.tf4.photospot.global.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.filter.details.ErrorResponseFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomExceptionFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (ApiException ex) {
			ErrorResponseFactory.create(request, response, ex);
		}
	}
}
