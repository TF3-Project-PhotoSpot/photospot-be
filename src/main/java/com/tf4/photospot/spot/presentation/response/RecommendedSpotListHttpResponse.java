package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;

@Builder
public record RecommendedSpotListHttpResponse(
	String centerAddress,
	String centerRoadAddress,
	List<RecommendedSpotHttpResponse> recommendedSpots,
	Boolean hasNext
) {
	public RecommendedSpotListHttpResponse {
		if (StringUtils.isEmpty(centerAddress) && StringUtils.isEmpty(centerRoadAddress)) {
			String recommendedSpotAddress = recommendedSpots.stream()
				.map(RecommendedSpotHttpResponse::address)
				.filter(StringUtils::isNotEmpty)
				.findFirst().orElseGet(() -> null);
			centerAddress = recommendedSpotAddress;
			centerRoadAddress = recommendedSpotAddress;
		}
	}

	public static RecommendedSpotListHttpResponse of(String centerAddress,
		RecommendedSpotListResponse recommendedSpotsResponse) {
		return RecommendedSpotListHttpResponse.builder()
			.centerAddress(centerAddress)
			.recommendedSpots(RecommendedSpotHttpResponse.convert(recommendedSpotsResponse.recommendedSpots()))
			.hasNext(recommendedSpotsResponse.hasNext())
			.build();
	}
}
