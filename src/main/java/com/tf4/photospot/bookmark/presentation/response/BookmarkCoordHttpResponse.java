package com.tf4.photospot.bookmark.presentation.response;

import com.tf4.photospot.bookmark.application.response.BookmarkCoord;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;

import lombok.Builder;

public record BookmarkCoordHttpResponse(
	Long bookmarkId,
	Long spotId,
	CoordinateDto coord
) {
	@Builder
	public BookmarkCoordHttpResponse {
	}

	public static BookmarkCoordHttpResponse from(BookmarkCoord bookmarkCoord) {
		return BookmarkCoordHttpResponse.builder()
			.bookmarkId(bookmarkCoord.bookmarkId())
			.spotId(bookmarkCoord.spotId())
			.coord(PointConverter.convert(bookmarkCoord.coord()))
			.build();
	}
}
