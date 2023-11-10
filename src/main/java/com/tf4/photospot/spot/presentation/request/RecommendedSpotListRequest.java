package com.tf4.photospot.spot.presentation.request;

import jakarta.validation.constraints.NotNull;

public record RecommendedSpotListRequest(
	@NotNull
	Double lat,
	@NotNull
	Double lon,
	@NotNull
	Long radius
) {
}
