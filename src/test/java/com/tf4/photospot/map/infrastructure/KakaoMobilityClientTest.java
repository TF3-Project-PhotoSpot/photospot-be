package com.tf4.photospot.map.infrastructure;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.tf4.photospot.global.config.maps.KakaoMapProperties;
import com.tf4.photospot.global.config.maps.MapApiConfig;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.response.kakao.KakaoDistanceResponse;
import com.tf4.photospot.support.RestClientTestSupport;

class KakaoMobilityClientTest extends RestClientTestSupport {
	private final MockRestServiceServer mockServer;
	private final KakaoMobilityClient kakaoMobilityClient;

	public KakaoMobilityClientTest(
		@Autowired RestClient.Builder restClientBuilder,
		@Autowired KakaoMapProperties properties
	) {
		mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
		kakaoMobilityClient = new MapApiConfig(properties).kakaoMobilityClient(restClientBuilder);
	}

	@DisplayName("좌표 사이의 거리를 검색한다.")
	@Test
	void test() {
		//given
		String startingCoord = PointConverter.toStringValue(
			PointConverter.convert(126.99599512792346, 35.976749396987046));
		String destCoord = PointConverter.toStringValue(
			PointConverter.convert(126.99597295767953, 35.97664845766847));

		String expectedUri = UriComponentsBuilder.fromHttpUrl("https://apis-navi.kakaomobility.com/v1/directions")
			.queryParam("origin", "{origin}")
			.queryParam("destination", "{destination}")
			.encode()
			.buildAndExpand(startingCoord, destCoord)
			.toUriString();

		final String expectedResponse = """
			{
				"trans_id": "c9d60cbabdd44deeafb1e78feb8c8b86",
				"routes": [
					{
						"result_code": 0,
						"result_msg": "길찾기 성공",
						"summary": {
							"origin": {
								"name": "",
								"x": 127.11015051307636,
								"y": 37.394725518530834
							},
							"destination": {
								"name": "",
								"x": 127.10823557165544,
								"y": 37.401928707331656
							},
							"waypoints": [],
							"priority": "DISTANCE",
							"bound": {
								"min_x": 127.10833536148644,
								"min_y": 37.39445954360996,
								"max_x": 127.1098222529551,
								"max_y": 37.40242724407785
							},
							"fare": {
								"taxi": 3800,
								"toll": 0
							},
							"distance": 1033,
							"duration": 349,
							"sections": [],
							"guides": []
						}
					}
				]
			}""";
		mockServer.expect(requestTo(expectedUri))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));
		//when
		KakaoDistanceResponse response = Assertions.assertDoesNotThrow(
			() -> kakaoMobilityClient.findDistance(startingCoord, destCoord));
		//then
		assertThat(response.getDistance()).isEqualTo(1033);
	}
}
