package com.tf4.photospot.spot.infrastructure.dto.kakao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Document(
	String addressName,
	String x,
	String y,
	KakaoRoadAddress roadAddress
) {
}
