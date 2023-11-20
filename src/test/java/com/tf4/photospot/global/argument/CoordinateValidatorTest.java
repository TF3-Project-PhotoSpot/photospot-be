package com.tf4.photospot.global.argument;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.tf4.photospot.global.dto.CoordinateDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/*
 * 	좌표에 대한 custom validator 검증 테스트
 * */
class CoordinateValidatorTest {
	private Validator validator;

	@BeforeEach
	public void setUp() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@DisplayName("유효한 좌표 범위가 들어오면 Validator를 통과한다.")
	@Test
	void coordinateValidSuccessTest() {
		//given
		CoordinateDto coordinateDto = new CoordinateDto(127.0, 37.0);
		//when
		Set<ConstraintViolation<CoordinateDto>> violations = validator.validate(coordinateDto);

		//then
		assertThat(violations).isEmpty();
	}

	@DisplayName("빈 값 또는 유효하지 않은 좌표가 들어오면 ConstraintViolation가 추가된다.")
	@MethodSource(value = "getCoordinateTestDatas")
	@ParameterizedTest(name = "{0}, validErrors {1}")
	void coordinateValidFailTest(CoordinateDto coordinateDto, Tuple... validErrors) {
		String fieldPath = "propertyPath.currentLeafNode.name";
		String errorMessagePath = "messageTemplate";
		// When
		Set<ConstraintViolation<CoordinateDto>> violations = validator.validate(coordinateDto);

		// Then
		assertThat(violations)
			.extracting(fieldPath, errorMessagePath)
			.contains(validErrors);

	}

	private static Stream<Arguments> getCoordinateTestDatas() {
		return Stream.of(
			Arguments.of(new CoordinateDto(null, 3711.0), new Tuple[] {
				new Tuple("lon", CoordinateValidator.COORD_NOT_EMPTY),
				new Tuple("lat", CoordinateValidator.COORD_INVALID_RANGE)
			}),
			Arguments.of(new CoordinateDto(null, null), new Tuple[] {
				new Tuple("lon", CoordinateValidator.COORD_NOT_EMPTY),
				new Tuple("lat", CoordinateValidator.COORD_NOT_EMPTY)
			}),
			Arguments.of(new CoordinateDto(127.0, 3711.0), new Tuple[] {
				new Tuple("lat", CoordinateValidator.COORD_INVALID_RANGE)
			}),
			Arguments.of(new CoordinateDto(-127.0, -37.0), new Tuple[] {
				new Tuple("lon", CoordinateValidator.COORD_INVALID_RANGE),
				new Tuple("lat", CoordinateValidator.COORD_INVALID_RANGE)
			})
		);
	}
}
