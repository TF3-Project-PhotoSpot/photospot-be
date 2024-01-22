package com.tf4.photospot.photo.presentation.request;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.util.PointConverter;

// Todo : 중심 좌표 validate 어떻게 할 건지
public record PostPhotoSaveRequest(

	String photoUrl,
	Double lon,
	Double lat,
	String takenAt
) {

	public Point toCoord() {
		return PointConverter.convert(lon, lat);
	}

	// 사진 찍인 날짜를 ISO 8601 형식으로 받는다고 가정
	public LocalDate toDate() {
		OffsetDateTime odt = OffsetDateTime.parse(takenAt);
		return odt.toLocalDate();
	}
}
