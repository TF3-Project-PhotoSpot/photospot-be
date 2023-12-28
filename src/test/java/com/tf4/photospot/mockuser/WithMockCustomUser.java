package com.tf4.photospot.mockuser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomerUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

	long id() default 1L;

	boolean hasLoggedInBefore() default false;

	String role() default "ROLE_USER";

}
