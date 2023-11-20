package com.tf4.photospot.spot.application;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
import com.tf4.photospot.spot.domain.MapApiClient;
import com.tf4.photospot.spot.domain.SpotRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpotService {
	private final MapApiClient mapApiClient;
	private final SpotRepository spotRepository;

	public RecommendedSpotsResponse getRecommendedSpotList(RecommendedSpotsRequest request) {
		return null;
	}

	public FindSpotResponse findSpot(FindSpotRequest request) {
		String address = mapApiClient.findAddressByCoordinate(request.coord())
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_ADDRESS_FOR_GIVEN_COORD));
		Point foundCoord = mapApiClient.findCoordinateByAddress(address)
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS));

		return spotRepository.findByCoord(foundCoord)
			.map(FindSpotResponse::toSpotResponse)
			.orElseGet(() -> FindSpotResponse.toNonSpotResponse(foundCoord, address));
	}
}
