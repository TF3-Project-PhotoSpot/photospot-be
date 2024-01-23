package com.tf4.photospot.map.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchAddressResponse;
import com.tf4.photospot.map.infrastructure.KakaoMapClient;
import com.tf4.photospot.support.IntegrationTestSupport;

class MapServiceTest extends IntegrationTestSupport {
	@Autowired
	private MapService mapService;

	@MockBean
	private KakaoMapClient kakaoMapClient;

	@TestFactory
	Stream<DynamicTest> searchByAddressStreamCountTest() {
		//given
		KakaoSearchAddressResponse response = KakaoSearchAddressResponse.builder()
			.meta(new KakaoSearchAddressResponse.Meta(1, 1, true))
			.documents(List.of(KakaoSearchAddressResponse.Document.builder()
				.address(KakaoSearchAddressResponse.Document.Address.builder()
					.addressName("전북 익산시 부송동 100")
					.x("126.99597295767953")
					.y("35.97664845766847").build())
				.roadAddress(KakaoSearchAddressResponse.Document.RoadAddress.builder()
					.addressName("전북 익산시 부송동 100")
					.x("126.99597295767953")
					.y("35.97664845766847").build()).build())
			).build();
		given(kakaoMapClient.searchAddress(anyString())).willReturn(response);

		return Stream.of(
			dynamicTest("지번 주소, 도로명 주소가 둘 다 유효하지 않을 경우 kakaoClient는 호출 되지 않는다.", () -> {
				//when
				catchException(() -> mapService.searchByAddress(null, ""))
					.addSuppressed(new ApiException(MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS));
				//then
				verify(kakaoMapClient, times(0)).searchAddress(anyString());
			}),
			dynamicTest("지번 주소, 도로명 주소가 둘 다 유효하지 않을 경우 NO_COORD_FOR_GIVEN_ADDRESS 예외가 발생한다", () -> {
				//when //then
				assertThatThrownBy(() -> mapService.searchByAddress(null, ""))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS);
			}),
			dynamicTest("지번 주소, 도로명 주소가 둘 다 유효할 경우 한번만 호출이 된다", () -> {
				//when
				mapService.searchByAddress("전북 익산시 부송동 100", "전북 익산시 부송동 100");
				//then
				verify(kakaoMapClient, atMostOnce()).searchAddress(anyString());
			})
		);
	}
}
