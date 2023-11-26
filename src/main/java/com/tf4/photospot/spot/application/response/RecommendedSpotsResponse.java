package com.tf4.photospot.spot.application.response;

import java.util.List;

import lombok.Builder;

@Builder
public record RecommendedSpotsResponse(
	String centerAddress,
	List<RecommendedSpotResponse> recommendedSpots
) {
}
