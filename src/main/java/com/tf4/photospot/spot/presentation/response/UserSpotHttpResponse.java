package com.tf4.photospot.spot.presentation.response;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.spot.application.response.SpotCoordResponse;

public record UserSpotHttpResponse(
	Long id,
	CoordinateDto coord
) {
	public static UserSpotHttpResponse from(SpotCoordResponse spotCoordResponse) {
		return new UserSpotHttpResponse(spotCoordResponse.id(), PointConverter.convert(spotCoordResponse.coord()));
	}
}
