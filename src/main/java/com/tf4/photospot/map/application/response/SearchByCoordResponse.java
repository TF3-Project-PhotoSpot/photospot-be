package com.tf4.photospot.map.application.response;

import com.tf4.photospot.map.application.response.kakao.KakaoCoordToAddressResponse;

import lombok.Builder;

@Builder
public record SearchByCoordResponse(
	String address,
	String roadAddress
) {
	public static SearchByCoordResponse from(KakaoCoordToAddressResponse kakaoResponse) {
		KakaoCoordToAddressResponse.Document document = kakaoResponse.findFirstDocument()
			.orElseGet(() -> KakaoCoordToAddressResponse.Document.DEFAULT_DOCUMENT);
		return SearchByCoordResponse.builder()
			.address(document.address().addressName())
			.roadAddress(document.roadAddress().addressName())
			.build();
	}
}
