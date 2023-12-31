package com.tf4.photospot.spot.application.response;

import java.util.List;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.domain.Spot;

import lombok.Builder;

@Builder
public record RecommendedSpotResponse(
	Long id,
	String address,
	Long postCount,
	CoordinateDto coord,
	List<PostPreviewResponse> postPreviewResponses
) {
	public static RecommendedSpotResponse of(Spot spot, List<PostPreviewResponse> postPreviewResponses) {
		return RecommendedSpotResponse.builder()
			.id(spot.getId())
			.address(spot.getAddress())
			.postCount(spot.getPostCount())
			.coord(PointConverter.convert(spot.getCoord()))
			.postPreviewResponses(postPreviewResponses)
			.build();
	}
}
