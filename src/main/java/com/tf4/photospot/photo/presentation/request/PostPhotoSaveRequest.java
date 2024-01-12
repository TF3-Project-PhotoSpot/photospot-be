package com.tf4.photospot.photo.presentation.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.util.PointConverter;

// Todo : 중심 좌표 validate 어떻게 할 건지
public record PostPhotoSaveRequest(
	Double lon,
	Double lat,
	String takenAt
) {

	public Point toCoord() {
		return PointConverter.convert(lon, lat);
	}

	public LocalDate toLocalDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		return LocalDate.parse(takenAt, formatter);
	}
}
