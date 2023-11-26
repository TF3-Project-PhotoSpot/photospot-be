package com.tf4.photospot.spot.application.response;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.spot.domain.Spot;

public record FindSpotResponse(
	Boolean isSpot,
	Long id,
	String address,
	CoordinateDto coord
) {
	public static FindSpotResponse toSpotResponse(Spot spot) {
		return new FindSpotResponse(Boolean.TRUE, spot.getId(), spot.getAddress(),
			PointConverter.convert(spot.getCoord()));
	}

	public static FindSpotResponse toNonSpotResponse(Point coord, String address) {
		return new FindSpotResponse(Boolean.FALSE, null, address, PointConverter.convert(coord));
	}
}
