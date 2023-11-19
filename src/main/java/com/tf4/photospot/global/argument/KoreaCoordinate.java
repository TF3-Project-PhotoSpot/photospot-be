package com.tf4.photospot.global.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = CoordinateValidator.class)
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface KoreaCoordinate {
	String message() default "잘못된 좌표입니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
