package com.tf4.photospot.spot.infrastructure;

import java.util.Optional;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.spot.domain.MapApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class KakaoMapApiClient implements MapApiClient {
	private final KakaoMapHttpExchange kakaoMapHttpExchange;

	@Override
	public Optional<String> findAddressByCoordinate(Point coord) {
		return kakaoMapHttpExchange.convertAddress(String.valueOf(coord.getX()), String.valueOf(coord.getY()))
			.findAddressName();
	}

	@Override
	public Optional<Point> findCoordinateByAddress(String address) {
		return kakaoMapHttpExchange.searchAddress(address).findCoordinate();
	}
}
