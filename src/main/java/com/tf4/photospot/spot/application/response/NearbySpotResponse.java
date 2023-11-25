package com.tf4.photospot.spot.application.response;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;

public record NearbySpotResponse(
	Long id,
	CoordinateDto coord
) {
	public NearbySpotResponse(Long id, Point coord) {
		this(id, PointConverter.convert(coord));
	}
}
