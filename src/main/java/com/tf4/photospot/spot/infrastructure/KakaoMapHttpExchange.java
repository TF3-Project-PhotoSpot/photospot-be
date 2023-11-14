package com.tf4.photospot.spot.infrastructure;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import com.tf4.photospot.spot.infrastructure.dto.kakao.KakaoAddressConvertResponse;
import com.tf4.photospot.spot.infrastructure.dto.kakao.KakaoSearchAddressResponse;

public interface KakaoMapHttpExchange {

	@GetExchange(value = "/geo/coord2address.json")
	KakaoAddressConvertResponse convertAddress(@RequestParam(name = "params") Map<String, String> params);

	@GetExchange("/search/address.json")
	KakaoSearchAddressResponse searchAddress(@RequestParam(name = "params") Map<String, String> params);
}
