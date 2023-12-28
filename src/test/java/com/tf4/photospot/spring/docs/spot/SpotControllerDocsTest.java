package com.tf4.photospot.spring.docs.spot;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchByCoordResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.NearbySpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.presentation.SpotController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class SpotControllerDocsTest extends RestDocsSupport {
	private final SpotService spotService = mock(SpotService.class);
	private final MapService mapService = mock(MapService.class);

	@Override
	protected Object initController() {
		return new SpotController(spotService, mapService);
	}

	@DisplayName("주변 추천 스팟 리스트를 조회한다.")
	@Test
	void getRecommendedSpotList() throws Exception {
		//given
		RecommendedSpotListResponse recommendedSpotsResponse = RecommendedSpotListResponse.builder()
			.recommendedSpots(List.of(createRecommendedSpotListResponse()))
			.hasNext(true)
			.build();
		given(mapService.searchByCoord(any(Point.class))).willReturn(createSearchByCoordResponse());
		given(spotService.getRecommendedSpotList(any(RecommendedSpotsRequest.class)))
			.willReturn(recommendedSpotsResponse);
		//when then
		mockMvc.perform(get("/api/v1/spots/recommended")
				.queryParam("lat", "37.66")
				.queryParam("lon", "127.04")
				.queryParam("radius", "200")
				.queryParam("postPreviewCount", "5")
				.queryParam("page", "0")
				.queryParam("size", "10")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("lat").description("위도")
						.attributes(coordConstraints()),
					parameterWithName("lon").description("경도")
						.attributes(coordConstraints()),
					parameterWithName("radius").description("반경")
						.attributes(constraints("반경(m)은 0보다 커야 됩니다.")),
					parameterWithName("postPreviewCount").description("미리보기 사진 개수")
						.optional()
						.attributes(constraints("미리보기 사진은 1~10개만 가능합니다."), defaultValue(5)),
					parameterWithName("page").description("페이지")
						.optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수")
						.optional()
						.attributes(defaultValue(10))
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("centerAddress").type(JsonFieldType.STRING).description("중심 좌표의 지면 주소"),
					fieldWithPath("centerRoadAddress").type(JsonFieldType.STRING).description("중심 좌표의 도로면 주소"),
					fieldWithPath("recommendedSpots").type(JsonFieldType.ARRAY).description("주변 추천 스팟 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("recommendedSpots[].id").type(JsonFieldType.NUMBER).description("스팟 ID"),
					fieldWithPath("recommendedSpots[].address").type(JsonFieldType.STRING).description("스팟 주소"),
					fieldWithPath("recommendedSpots[].postCount").type(JsonFieldType.NUMBER).description("스팟 방명록수"),
					fieldWithPath("recommendedSpots[].coord").type(JsonFieldType.OBJECT).description("스팟 좌표"),
					fieldWithPath("recommendedSpots[].coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("recommendedSpots[].coord.lon").type(JsonFieldType.NUMBER).description("경도"),
					fieldWithPath("recommendedSpots[].photoUrls").type(JsonFieldType.ARRAY).description("최신 방명록 사진")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부")
				)));

	}

	@DisplayName("반경 내에 위치한 주변 스팟을 조회한다.")
	@Test
	void getNearbySpots() throws Exception {
		//given
		given(spotService.getNearbySpotList(any(NearbySpotRequest.class))).willReturn(new NearbySpotListResponse(
			List.of(new NearbySpotResponse(1L, new CoordinateDto(127.0468177, 37.6676198)))));
		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lat", "37.6676198")
				.queryParam("lon", "127.0468177")
				.queryParam("radius", "5000"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("lat").description("위도")
						.attributes(coordConstraints()),
					parameterWithName("lon").description("경도")
						.attributes(coordConstraints()),
					parameterWithName("radius").description("반경")
						.attributes(constraints("반경(m)은 0보다 커야 됩니다."))
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("spots[].id").type(JsonFieldType.NUMBER).description("스팟 id"),
					fieldWithPath("spots[].coord").type(JsonFieldType.OBJECT).description("스팟 좌표 정보"),
					fieldWithPath("spots[].coord.lat").type(JsonFieldType.NUMBER).description("스팟 위도"),
					fieldWithPath("spots[].coord.lon").type(JsonFieldType.NUMBER).description("스팟 경도")
				))
			);
	}

	private static SearchByCoordResponse createSearchByCoordResponse() {
		return SearchByCoordResponse.builder()
			.address("서울시 도봉구 마들로 643")
			.roadAddress("서울시 도봉구 마들로 643")
			.build();
	}

	private static RecommendedSpotResponse createRecommendedSpotListResponse() {
		return RecommendedSpotResponse.builder()
			.id(1L)
			.address("서울시 도봉구 마들로 643")
			.postCount(10L)
			.coord(new CoordinateDto(32.0000, 70.0000))
			.postPreviewResponses(List.of(
				new PostPreviewResponse(3L, 1L, "http://image3.com")))
			.build();
	}
}
