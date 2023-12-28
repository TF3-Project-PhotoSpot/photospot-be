package com.tf4.photospot.map.application.response;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchAddressResponse;

import lombok.Builder;

@Builder
public record SearchByAddressResponse(
	String address,
	CoordinateDto addressCoord,
	String roadAddress,
	CoordinateDto roadAddressCoord
) {
	public static SearchByAddressResponse from(KakaoSearchAddressResponse kakaoResponse) {
		KakaoSearchAddressResponse.Document document = kakaoResponse.findFirstDocument()
			.orElseGet(() -> KakaoSearchAddressResponse.Document.DEFAULT_DOCUMENT);
		SearchByAddressResponseBuilder responseBuilder = SearchByAddressResponse.builder();
		responseBuilder.address(document.address().addressName());
		responseBuilder.roadAddress(document.roadAddress().addressName());
		CoordinateDto.parse(document.address().x(), document.address().y())
			.ifPresent(responseBuilder::addressCoord);
		CoordinateDto.parse(document.roadAddress().x(), document.roadAddress().y())
			.ifPresent(responseBuilder::roadAddressCoord);
		return responseBuilder.build();
	}
}
