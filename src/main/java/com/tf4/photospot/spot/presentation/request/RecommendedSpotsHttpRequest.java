package com.tf4.photospot.spot.presentation.request;

import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;

import jakarta.validation.constraints.NotNull;

public record RecommendedSpotsHttpRequest(
	@NotNull
	Double lat,
	@NotNull
	Double lon,
	@NotNull
	Long radius
) {
	public RecommendedSpotsRequest toServiceRequest() {
		return new RecommendedSpotsRequest(PointConverter.convert(lat, lon), radius);
	}
}
