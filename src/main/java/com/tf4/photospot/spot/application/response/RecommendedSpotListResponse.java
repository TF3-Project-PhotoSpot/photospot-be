package com.tf4.photospot.spot.application.response;

import java.util.List;

import lombok.Builder;

@Builder
public record RecommendedSpotListResponse(
	String centerAddress,
	List<RecommendedSpot> recommendedSpots
) {
}
