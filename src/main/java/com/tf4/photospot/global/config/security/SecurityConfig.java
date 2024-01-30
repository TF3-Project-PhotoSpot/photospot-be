package com.tf4.photospot.global.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import com.tf4.photospot.auth.application.JwtService;
import com.tf4.photospot.global.filter.CustomAuthenticationFilter;
import com.tf4.photospot.global.filter.CustomExceptionFilter;
import com.tf4.photospot.global.filter.JwtTokenValidatorFilter;
import com.tf4.photospot.global.filter.details.CustomAuthenticationEntryPoint;
import com.tf4.photospot.global.filter.details.CustomAuthenticationProvider;
import com.tf4.photospot.global.filter.details.CustomAuthenticationSuccessHandler;
import com.tf4.photospot.user.application.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtService jwtService;
	private final UserService userService;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		// 정적 리소스 경로에 대해서 시큐리티 필터 제외
		return (web -> web.ignoring()
			.requestMatchers("/docs/index.html")
			.requestMatchers(PathRequest.toStaticResources().atCommonLocations()));
	}

	// Todo : Https 연결 시 CSRF 활성화 & PermitAll 수정
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName("_csrf");
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.cors(corsCustomizer -> corsCustomizer.disable())
			.csrf(csrf -> csrf.csrfTokenRequestHandler(requestHandler)
				.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.disable())
			.addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(new JwtTokenValidatorFilter(jwtService), CustomAuthenticationFilter.class)
			.addFilterBefore(new CustomExceptionFilter(), JwtTokenValidatorFilter.class)
			// .authorizeHttpRequests(requests -> requests
			// 	.anyRequest().permitAll())
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
		return http.build();
	}

	@Bean
	public CustomAuthenticationFilter customAuthenticationFilter() {
		CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager());
		customAuthenticationFilter.setFilterProcessesUrl(SecurityConstant.LOGIN_URL);
		customAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
		return customAuthenticationFilter;
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(customAuthenticationProvider());
	}

	@Bean
	public CustomAuthenticationProvider customAuthenticationProvider() {
		return new CustomAuthenticationProvider(userService);
	}

	@Bean
	public CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
		return new CustomAuthenticationSuccessHandler(jwtService);
	}

}
