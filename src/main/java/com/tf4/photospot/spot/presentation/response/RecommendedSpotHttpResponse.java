package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;

import lombok.Builder;

@Builder
public record RecommendedSpotHttpResponse(
	Long id,
	String address,
	Long postCount,
	CoordinateDto coord,
	List<String> photoUrls
) {
	public static List<RecommendedSpotHttpResponse> convert(List<RecommendedSpotResponse> responses) {
		return responses.stream()
			.map(response -> RecommendedSpotHttpResponse.builder()
				.id(response.id())
				.address(response.address())
				.postCount(response.postCount())
				.coord(response.coord())
				.photoUrls(response.postPreviewResponses().stream()
					.map(PostPreviewResponse::photoUrl)
					.toList())
				.build())
			.toList();
	}
}
