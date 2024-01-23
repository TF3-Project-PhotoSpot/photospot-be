package com.tf4.photospot.map.infrastructure;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.tf4.photospot.map.application.response.kakao.KakaoDistanceResponse;

@HttpExchange("https://apis-navi.kakaomobility.com")
public interface KakaoMobilityClient {
	@GetExchange(value = "/v1/directions")
	KakaoDistanceResponse findDistance(
		@RequestParam(name = "origin") String startingCoord,
		@RequestParam(name = "destination") String destCoord
	);
}
