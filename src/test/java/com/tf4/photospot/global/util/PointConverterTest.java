package com.tf4.photospot.global.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

class PointConverterTest {
	@DisplayName("좌표를 문자열 포맷으로 바꾼다.")
	@Test
	void parseStringValue() {
		//given
		Point coord = PointConverter.convert(127.1234, 34.1234);
		//when
		String stringValue = PointConverter.toStringValue(coord);
		//then
		Assertions.assertThat("127.1234,34.1234").isEqualTo(stringValue);
	}
}