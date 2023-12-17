package com.tf4.photospot.map.infrastructure;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import com.tf4.photospot.map.application.response.kakao.KakaoCoordToAddressResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchAddressResponse;

public interface KakaoMapClient {

	@GetExchange(value = "/geo/coord2address.json")
	KakaoCoordToAddressResponse convertCoordToAddress(
		@RequestParam(name = "x") String lon, @RequestParam(name = "y") String lat);

	@GetExchange("/search/address.json")
	KakaoSearchAddressResponse searchAddress(@RequestParam(name = "query") String address);
}
