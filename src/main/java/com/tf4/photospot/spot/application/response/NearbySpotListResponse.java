package com.tf4.photospot.spot.application.response;

import java.util.List;

public record NearbySpotListResponse(
	List<NearbySpotResponse> spots
) {
}
