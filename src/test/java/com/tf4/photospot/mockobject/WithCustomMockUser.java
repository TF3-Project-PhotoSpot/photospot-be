package com.tf4.photospot.mockobject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

import com.tf4.photospot.user.domain.Role;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomerMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {

	long userId() default 1L;

	boolean hasLoggedInBefore() default false;

	Role role() default Role.USER;

}
