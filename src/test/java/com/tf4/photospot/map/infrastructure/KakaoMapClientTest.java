package com.tf4.photospot.map.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.util.UriComponentsBuilder;

import com.tf4.photospot.global.config.maps.KakaoMapProperties;
import com.tf4.photospot.global.config.maps.MapApiConfig;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.response.kakao.KakaoCoordToAddressResponse;
import com.tf4.photospot.map.application.response.kakao.KakaoSearchAddressResponse;

@EnableConfigurationProperties(value = KakaoMapProperties.class)
@TestPropertySource("classpath:application.yml")
@RestClientTest(value = MapApiConfig.class)
class KakaoMapClientTest {
	@Autowired
	private KakaoMapClient kakaoMapClient;

	@Autowired
	private KakaoMapProperties properties;

	@Autowired
	private MockRestServiceServer mockServer;

	@DisplayName("주소로 지역을 검색한다.")
	@Test
	void searchAddress() {
		//given
		String expectedUri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/search/address.json")
			.queryParam("query", "전북 삼성동 100")
			.encode(StandardCharsets.UTF_8)
			.build().toUriString();
		CoordinateDto expectedRoadAddressCoord = new CoordinateDto(126.99599512792346, 35.976749396987046);
		CoordinateDto expecteAddressCoord = new CoordinateDto(126.99597295767953, 35.97664845766847);
		String expectedResponse = """
			{
				"meta": {
					"total_count": 4,
					"pageable_count": 4,
					"is_end": true
				},
				"documents": [
					{
						"address_name": "전북 익산시 부송동 100",
						"y": "35.97664845766847",
						"x": "126.99597295767953",
						"address_type": "REGION_ADDR",
						"address": {
							"address_name": "전북 익산시 부송동 100",
							"region_1depth_name": "전북",
							"region_2depth_name": "익산시",
							"region_3depth_name": "부송동",
							"region_3depth_h_name": "삼성동",
							"x": "126.99597295767953",
							"y": "35.97664845766847"
						},
						"road_address": {
							"address_name": "전북 익산시 망산길 11-17",
							"region_1depth_name": "전북",
							"region_2depth_name": "익산시",
							"region_3depth_name": "부송동",
							"y": "35.976749396987046",
							"x": "126.99599512792346"
						}
					}
				]
			}
			""";
		mockServer.expect(requestTo(expectedUri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));
		//when
		KakaoSearchAddressResponse response = Assertions.assertDoesNotThrow(
			() -> kakaoMapClient.searchAddress("전북 삼성동 100"));
		//then
		assertThat(response.findFirstDocument())
			.isPresent().get()
			.extracting("address.x", "address.y", "roadAddress.x", "roadAddress.y")
			.containsExactly(
				String.valueOf(expecteAddressCoord.lon()),
				String.valueOf(expecteAddressCoord.lat()),
				String.valueOf(expectedRoadAddressCoord.lon()),
				String.valueOf(expectedRoadAddressCoord.lat())
			);
	}

	@DisplayName("좌표를 지도상 주소로 변환한다.")
	@Test
	void convertCoordToAddress() {
		//given
		String expectedUri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl() + "/geo/coord2address.json")
			.queryParam("x", "127.0")
			.queryParam("y", "37.0")
			.encode(StandardCharsets.UTF_8)
			.build().toUriString();
		String expectedAddress = "경기도 안성시 죽산면 죽산초교길 69-4";
		String expectedResponse = """
			{
				"meta": {
					"total_count": 1
				},
				"documents": [
					{
						"road_address": {
							"address_name": "경기도 안성시 죽산면 죽산초교길 69-4",
							"region_1depth_name": "경기",
							"region_2depth_name": "안성시",
							"region_3depth_name": "죽산면",
							"road_name": "죽산초교길",
							"building_name": "무지개아파트"
						},
						"address": {
							"address_name": "경기 안성시 죽산면 죽산리 343-1",
							"region_1depth_name": "경기",
							"region_2depth_name": "안성시",
							"region_3depth_name": "죽산면 죽산리"
						}
					}
				]
			}
			""";
		mockServer.expect(requestTo(expectedUri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));
		//when
		KakaoCoordToAddressResponse response = Assertions.assertDoesNotThrow(
			() -> kakaoMapClient.convertCoordToAddress(127.0, 37.0));

		//then
		assertThat(response.findFirstDocument()).isPresent().get()
			.extracting("roadAddress.addressName", "address.addressName")
			.containsExactly(
				"경기도 안성시 죽산면 죽산초교길 69-4",
				"경기 안성시 죽산면 죽산리 343-1"
			);

	}
}
