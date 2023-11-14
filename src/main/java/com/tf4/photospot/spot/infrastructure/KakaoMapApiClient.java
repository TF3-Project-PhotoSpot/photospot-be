package com.tf4.photospot.spot.infrastructure;

import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import com.tf4.photospot.spot.domain.MapApiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMapApiClient implements MapApiClient {
	private final KakaoMapHttpExchange kakaoMapHttpExchange;

	@Override
	public Optional<String> findAddressByCoordinate(Point coord) {
		var requestParams = Map.of(
			"x", String.valueOf(coord.getX()),
			"y", String.valueOf(coord.getY())
		);
		return kakaoMapHttpExchange.convertAddress(requestParams).findAddressName();

	}

	@Override
	public Optional<Point> findCoordinateByAddress(String address) {
		var requestParams = Map.of("query", address);
		return kakaoMapHttpExchange.searchAddress(requestParams).findCoordinate();
	}
}
