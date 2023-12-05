package com.tf4.photospot.spot.application.response;

import java.util.Collections;
import java.util.List;

import lombok.Builder;

@Builder
public record RecommendedSpotsResponse(
	List<RecommendedSpotResponse> recommendedSpots,
	Boolean hasNext
) {
	public static RecommendedSpotsResponse emptyResponse() {
		return new RecommendedSpotsResponse(Collections.emptyList(), false);
	}
}
