package com.tf4.photospot.map.presentation.response;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;

public record SearchLocationHttpResponse(
	String address,
	CoordinateDto coord
) {
	public static SearchLocationHttpResponse of(String address, Point coord) {
		return new SearchLocationHttpResponse(address, PointConverter.convert(coord));
	}
}
