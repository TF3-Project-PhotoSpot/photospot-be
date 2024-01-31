package com.tf4.photospot.photo.presentation.request;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.tf4.photospot.global.argument.KoreaCoordinate;
import com.tf4.photospot.global.dto.CoordinateDto;

public record PhotoSaveHttpRequest(

	String photoUrl,

	@KoreaCoordinate
	CoordinateDto coord,

	String takenAt
) {

	// 사진 찍인 날짜를 ISO 8601 형식으로 받는다고 가정
	public LocalDate toDate() {
		OffsetDateTime odt = OffsetDateTime.parse(takenAt);
		return odt.toLocalDate();
	}
}
