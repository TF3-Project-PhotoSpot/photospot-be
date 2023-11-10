package com.tf4.photospot.spot.application;

import org.springframework.stereotype.Service;

import com.tf4.photospot.spot.application.request.RecommendedSpotListServiceRequest;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpotService {

	public RecommendedSpotListResponse getRecommendedSpotList(RecommendedSpotListServiceRequest request) {
		return null;
	}
}
