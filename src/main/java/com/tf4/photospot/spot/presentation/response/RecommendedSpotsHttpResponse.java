package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;

import lombok.Builder;

@Builder
public record RecommendedSpotsHttpResponse(
	String centerAddress,
	List<RecommendedSpotHttpResponse> recommendedSpots,
	Boolean hasNext
) {
	public static RecommendedSpotsHttpResponse of(String centerAddress,
		RecommendedSpotsResponse recommendedSpotsResponse) {
		return RecommendedSpotsHttpResponse.builder()
			.centerAddress(centerAddress)
			.recommendedSpots(RecommendedSpotHttpResponse.convert(recommendedSpotsResponse.recommendedSpots()))
			.hasNext(recommendedSpotsResponse.hasNext())
			.build();
	}
}
