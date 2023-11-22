package com.tf4.photospot.spot.application.response;

import java.util.List;

import com.tf4.photospot.global.dto.CoordinateDto;

import lombok.Builder;

@Builder
public record RecommendedSpotResponse(
	Long id,
	String address,
	Long postCount,
	CoordinateDto coord,
	List<String> photoUrls
) {
}
