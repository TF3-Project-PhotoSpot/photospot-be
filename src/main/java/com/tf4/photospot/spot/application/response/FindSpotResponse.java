package com.tf4.photospot.spot.application.response;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.spot.domain.Spot;

public record FindSpotResponse(
	Boolean isSpot,
	Long id,
	CoordinateDto coord
) {
	public static FindSpotResponse toSpotResponse(Spot spot) {
		return new FindSpotResponse(Boolean.TRUE, spot.getId(), PointConverter.convert(spot.getCoord()));
	}

	public static FindSpotResponse toNonSpotResponse(Point coord) {
		return new FindSpotResponse(Boolean.FALSE, null, PointConverter.convert(coord));
	}
}
