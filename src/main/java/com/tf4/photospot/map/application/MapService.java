package com.tf4.photospot.map.application;

import java.util.stream.Stream;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.response.SearchByAddressResponse;
import com.tf4.photospot.map.application.response.SearchByCoordResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoDistanceResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchAddressResponse;
import com.tf4.photospot.map.infrastructure.KakaoMapClient;
import com.tf4.photospot.map.infrastructure.KakaoMobilityClient;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {
	private final KakaoMapClient kakaoMapClient;
	private final KakaoMobilityClient kakaoMobilityClient;

	public SearchByCoordResponse searchByCoord(Point coord) {
		return SearchByCoordResponse.from(kakaoMapClient.convertCoordToAddress(coord.getX(), coord.getY()));
	}

	public SearchByAddressResponse searchByAddress(String address, String roadAddress) {
		KakaoSearchAddressResponse response = Stream.of(address, roadAddress)
			.filter(StringUtils::isNotEmpty)
			.map(kakaoMapClient::searchAddress)
			.filter(KakaoSearchAddressResponse::existResult)
			.findFirst()
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS));
		return SearchByAddressResponse.from(response);
	}

	public Integer searchDistanceBetween(Point startingCoord, Point destCoord) {
		KakaoDistanceResponse response = kakaoMobilityClient.findDistance(
			PointConverter.toStringValue(startingCoord), PointConverter.toStringValue(destCoord));
		return response.getDistance();
	}
}
