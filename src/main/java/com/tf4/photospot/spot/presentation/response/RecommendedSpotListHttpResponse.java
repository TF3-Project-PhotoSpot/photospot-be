package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;

import lombok.Builder;

@Builder
public record RecommendedSpotListHttpResponse(
	String centerAddress,
	List<RecommendedSpotHttpResponse> recommendedSpots,
	Boolean hasNext
) {
	public static RecommendedSpotListHttpResponse of(String centerAddress,
		RecommendedSpotListResponse recommendedSpotsResponse) {
		return RecommendedSpotListHttpResponse.builder()
			.centerAddress(centerAddress)
			.recommendedSpots(RecommendedSpotHttpResponse.convert(recommendedSpotsResponse.recommendedSpots()))
			.hasNext(recommendedSpotsResponse.hasNext())
			.build();
	}
}
