package com.tf4.photospot.spot.application.response;

import java.util.List;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.domain.Spot;

import lombok.Builder;

public record SpotResponse(
	Long id,
	String address,
	Point coord,
	Long postCount,
	List<PostPreviewResponse> previewResponses,
	Boolean bookmarked
) {
	@Builder
	public SpotResponse {
		if (postCount == null) {
			postCount = 0L;
		}
	}

	public static SpotResponse of(Spot spot, Boolean bookmarked, List<PostPreviewResponse> previewResponses) {
		return new SpotResponse(
			spot.getId(),
			spot.getAddress(),
			spot.getCoord(),
			spot.getPostCount(),
			previewResponses,
			bookmarked
		);
	}
}
