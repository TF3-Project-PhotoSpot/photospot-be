package com.tf4.photospot.map.application;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.map.infrastructure.KakaoMapClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapService {
	private final KakaoMapClient kakaoMapClient;

	public String searchAddress(Point coord) {
		return kakaoMapClient.convertCoordToAddress(coord.getX(), coord.getY())
			.findAddressName()
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_ADDRESS_FOR_GIVEN_COORD));
	}

	public Point searchCoordinate(String address) {
		return kakaoMapClient.searchAddress(address)
			.findCoordinate()
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS));
	}
}
