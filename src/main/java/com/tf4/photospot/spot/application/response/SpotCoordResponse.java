package com.tf4.photospot.spot.application.response;

import org.locationtech.jts.geom.Point;

import com.querydsl.core.annotations.QueryProjection;

public record SpotCoordResponse(
	Long id,
	Point coord
) {
	@QueryProjection
	public SpotCoordResponse {
	}
}
