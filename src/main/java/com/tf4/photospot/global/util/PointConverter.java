package com.tf4.photospot.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import com.tf4.photospot.global.dto.CoordinateDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointConverter {
	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	public static Point convert(final Double lon, final Double lat) {
		return geometryFactory.createPoint(new Coordinate(lon, lat));
	}

	public static Point convert(final CoordinateDto dto) {
		return geometryFactory.createPoint(new Coordinate(dto.lon(), dto.lat()));
	}

	public static CoordinateDto convert(final Point coord) {
		return new CoordinateDto(coord.getX(), coord.getY());
	}

	public static String toStringValue(final Point coord) {
		return coord.getX() + "," + coord.getY();
	}
}
