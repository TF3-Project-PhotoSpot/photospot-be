package com.tf4.photospot.map.presentation.response;

import com.tf4.photospot.global.dto.CoordinateDto;

import lombok.Builder;

@Builder
public record SearchLocationHttpResponse(
	Boolean isExist,
	String address,
	String roadAddress,
	CoordinateDto coord
) {
	public SearchLocationHttpResponse {
		isExist = (coord != null);
	}
}
