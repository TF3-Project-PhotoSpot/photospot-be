package com.tf4.photospot.global.dto;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.constraint.Coordinate;
import com.tf4.photospot.global.util.PointConverter;

@Coordinate
public record CoordinateDto(
	Double lon,
	Double lat
) {
	public Point toCoord() {
		return PointConverter.convert(this);
	}
}
