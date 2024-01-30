package com.tf4.photospot.spot.presentation.response;

import java.util.List;

import com.tf4.photospot.spot.application.response.SpotCoordResponse;

public record UserSpotListHttpResponse(
	List<UserSpotHttpResponse> spots
) {
	public static UserSpotListHttpResponse from(List<SpotCoordResponse> spotCoordResponses) {
		return new UserSpotListHttpResponse(spotCoordResponses.stream()
			.map(UserSpotHttpResponse::from)
			.toList());
	}
}
