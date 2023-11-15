package com.tf4.photospot.spot.application;

import org.springframework.stereotype.Service;

import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
import com.tf4.photospot.spot.domain.MapApiClient;
import com.tf4.photospot.spot.domain.SpotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpotService {
	private final MapApiClient mapApiClient;
	private final SpotRepository spotRepository;

	public RecommendedSpotsResponse getRecommendedSpotList(RecommendedSpotsRequest request) {
		return null;
	}

	public FindSpotResponse findSpot(FindSpotRequest request) {
		var address = mapApiClient.findAddressByCoordinate(request.coord()).orElseThrow();
		var foundCoord = mapApiClient.findCoordinateByAddress(address).orElseThrow();

		return spotRepository.findByCoord(foundCoord)
			.map(FindSpotResponse::toSpotResponse)
			.orElseGet(() -> FindSpotResponse.toNonSpotResponse(foundCoord));
	}
}
