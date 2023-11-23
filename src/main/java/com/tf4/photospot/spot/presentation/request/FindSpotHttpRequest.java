package com.tf4.photospot.spot.presentation.request;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.util.PointConverter;

import jakarta.validation.constraints.NotNull;

public record FindSpotHttpRequest(
	@NotNull
	Double lat,
	@NotNull
	Double lon
) {
	public Point toCoord() {
		return PointConverter.convert(lat, lon);
	}
}
