package com.tf4.photospot.post.presentation.request;

import java.time.LocalDateTime;

import com.tf4.photospot.global.argument.KoreaCoordinate;
import com.tf4.photospot.global.dto.CoordinateDto;

public record PhotoInfoDto(
	String photoUrl,

	@KoreaCoordinate
	CoordinateDto coord,

	String takenAt
) {

	public LocalDateTime toDate() {
		return LocalDateTime.parse(takenAt);
	}
}
