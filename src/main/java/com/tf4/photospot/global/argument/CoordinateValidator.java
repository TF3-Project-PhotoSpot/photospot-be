package com.tf4.photospot.global.argument;

import com.tf4.photospot.global.dto.CoordinateDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/*
 * WGS84 좌표
 * 한국
 * 위도 약 33 ~ 39
 * 경도 약 124 ~ 132
 * */
public class CoordinateValidator implements ConstraintValidator<KoreaCoordinate, CoordinateDto> {
	public static final String COORD_NOT_EMPTY = "좌표에 빈 값이 들어갈 수 없습니다.";
	public static final String COORD_INVALID_RANGE = "좌표의 범위가 유효하지 않습니다.";
	private static final double MIN_LATITUDE = 33.0;
	private static final double MAX_LATITUDE = 39.0;
	private static final double MIN_LONGITUDE = 124.0;
	private static final double MAX_LONGITUDE = 132.0;

	@Override
	public boolean isValid(CoordinateDto value, ConstraintValidatorContext context) {
		boolean isValid = true;
		final Double lon = value.lon();
		final Double lat = value.lat();
		if (lon == null) {
			addConstraintViolationMessage(context, "lon", COORD_NOT_EMPTY);
			isValid = false;
		} else if (lon < MIN_LONGITUDE || lon > MAX_LONGITUDE) {
			addConstraintViolationMessage(context, "lon", COORD_INVALID_RANGE);
			isValid = false;
		}
		if (lat == null) {
			addConstraintViolationMessage(context, "lat", COORD_NOT_EMPTY);
			isValid = false;
		} else if (lat < MIN_LATITUDE || lat > MAX_LATITUDE) {
			addConstraintViolationMessage(context, "lat", COORD_INVALID_RANGE);
			isValid = false;
		}

		return isValid;
	}

	private void addConstraintViolationMessage(ConstraintValidatorContext context, String field, String message) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addPropertyNode(field)
			.addConstraintViolation();
	}
}
