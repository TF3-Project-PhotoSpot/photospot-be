package com.tf4.photospot.post.presentation.request;

import com.tf4.photospot.global.argument.KoreaCoordinate;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.spot.domain.Spot;

public record SpotInfoDto(
	@KoreaCoordinate
	CoordinateDto coord,

	String address
) {

	public Spot toSpot() {
		return Spot.builder()
			.coord(coord.toCoord())
			.address(address)
			.build();
	}
}
