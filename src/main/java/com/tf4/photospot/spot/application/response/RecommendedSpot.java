package com.tf4.photospot.spot.application.response;

import java.util.List;

import com.tf4.photospot.global.dto.CoordinateDto;

import lombok.Builder;

@Builder
public record RecommendedSpot(
	Long id,
	String address,
	Long bookmarkedCount,
	CoordinateDto coordinate,
	List<String> photoUrls
) {
}
