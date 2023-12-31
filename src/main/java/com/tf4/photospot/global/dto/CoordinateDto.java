package com.tf4.photospot.global.dto;

import java.util.Optional;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.argument.KoreaCoordinate;
import com.tf4.photospot.global.util.PointConverter;

@KoreaCoordinate
public record CoordinateDto(
	Double lon,
	Double lat
) {
	public Point toCoord() {
		return PointConverter.convert(this);
	}

	public static Optional<CoordinateDto> parse(String lon, String lat) {
		try {
			return Optional.of(new CoordinateDto(Double.valueOf(lon), Double.valueOf(lat)));
		} catch (NullPointerException | NumberFormatException ex) {
			return Optional.empty();
		}
	}
}
