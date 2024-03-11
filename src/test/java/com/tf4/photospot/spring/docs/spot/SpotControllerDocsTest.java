package com.tf4.photospot.spring.docs.spot;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchByCoordResponse;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.MostPostTagRank;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.NearbySpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.application.response.SpotCoordResponse;
import com.tf4.photospot.spot.application.response.SpotResponse;
import com.tf4.photospot.spot.presentation.SpotController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class SpotControllerDocsTest extends RestDocsSupport {
	private static final CoordinateDto DEFAULT_COORD = new CoordinateDto(127.0468177, 37.6676198);

	private final SpotService spotService = mock(SpotService.class);
	private final MapService mapService = mock(MapService.class);

	@Override
	protected Object initController() {
		return new SpotController(spotService, mapService);
	}

	@DisplayName("내가 작성한 방명록의 스팟 목록을 조회한다.")
	@Test
	void getSpotsOfMyPosts() throws Exception {
		//given
		final List<SpotCoordResponse> spotCoordResponses = List.of(
			new SpotCoordResponse(1L, DEFAULT_COORD.toCoord()),
			new SpotCoordResponse(2L, DEFAULT_COORD.toCoord()));
		given(spotService.findSpotsOfMyPosts(anyLong())).willReturn(spotCoordResponses);
		//when then
		mockMvc.perform(get("/api/v1/spots/mine", 1L))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("spots[]").type(JsonFieldType.ARRAY).description("스팟 목록")
						.optional().attributes(defaultValue("emptyList")),
					fieldWithPath("spots[].id").type(JsonFieldType.NUMBER).description("스팟 ID"),
					fieldWithPath("spots[].coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("spots[].coord.lon").type(JsonFieldType.NUMBER).description("경도")
				)));
	}

	@DisplayName("스팟을 조회한다.")
	@Test
	void getSpot() throws Exception {
		//given
		SpotResponse spotResponse = createSpotResponse();
		given(mapService.searchDistanceBetween(any(Point.class), any(Point.class))).willReturn(100);
		given(spotService.findSpot(any(PostSearchCondition.class), anyInt())).willReturn(spotResponse);
		//when
		mockMvc.perform(get("/api/v1/spots/{spotId}", spotResponse.id())
				.queryParam("lon", String.valueOf(DEFAULT_COORD.lon()))
				.queryParam("lat", String.valueOf(DEFAULT_COORD.lat()))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("spotId").description("스팟 ID")),
				queryParameters(
					parameterWithName("lat").description("위도").attributes(coordConstraints()),
					parameterWithName("lon").description("경도").attributes(coordConstraints()),
					parameterWithName("postPreviewCount").description("미리보기 사진 개수")
						.optional().attributes(constraints("미리보기 사진은 1~10개만 가능합니다."), defaultValue(5)),
					parameterWithName("mostPostTagCount").description("태그 통계 개수")
						.optional().attributes(constraints("태그 통계는 1~5개만 가능합니다."), defaultValue(3)),
					parameterWithName("distance").description("거리 정보 필요 여부")
						.optional().attributes(defaultValue("true")),
					parameterWithName("page").description("페이지")
						.optional().attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10))
				),
				responseFields(
					fieldWithPath("distance").type(JsonFieldType.NUMBER).description("현재 위치에서 스팟 거리")
						.attributes(defaultValue(0)),
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("스팟 ID"),
					fieldWithPath("address").type(JsonFieldType.STRING).description("스팟 주소"),
					fieldWithPath("coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("coord.lon").type(JsonFieldType.NUMBER).description("경도"),
					fieldWithPath("postCount").type(JsonFieldType.NUMBER).description("방명록 개수"),
					fieldWithPath("tags").type(JsonFieldType.ARRAY).description("태그 통계"),
					fieldWithPath("tags[].id").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("tags[].count").type(JsonFieldType.NUMBER).description("태그 개수"),
					fieldWithPath("tags[].name").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("tags[].iconUrl").type(JsonFieldType.STRING).description("태그 아이콘 url"),
					fieldWithPath("photoUrls").type(JsonFieldType.ARRAY).description("최신 방명록 사진")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("bookmarked").type(JsonFieldType.BOOLEAN).description("북마크 등록 여부")
				)));
	}

	private static SpotResponse createSpotResponse() {
		return SpotResponse.builder()
			.id(1L)
			.address("주소")
			.coord(DEFAULT_COORD.toCoord())
			.postCount(2L)
			.previewResponses(List.of(
				new PostPreviewResponse(1L, 2L, "photoUrl2"),
				new PostPreviewResponse(1L, 1L, "photoUrl1")))
			.mostPostTagRanks(List.of(
				MostPostTagRank.builder().id(1L).count(5).name("tagName").iconUrl("iconUrl").build(),
				MostPostTagRank.builder().id(2L).count(10).name("tagName2").iconUrl("iconUrl2").build()
			))
			.bookmarked(false)
			.build();
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
				.queryParam("lat", String.valueOf(DEFAULT_COORD.lat()))
				.queryParam("lon", String.valueOf(DEFAULT_COORD.lon()))
				.queryParam("radius", "200")
				.queryParam("postPreviewCount", "5")
				.queryParam("page", "0")
				.queryParam("size", "10")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("lat").description("위도").attributes(coordConstraints()),
					parameterWithName("lon").description("경도").attributes(coordConstraints()),
					parameterWithName("radius").description("반경").attributes(constraints("반경(m)은 0보다 커야 됩니다.")),
					parameterWithName("postPreviewCount").description("미리보기 사진 개수")
						.optional().attributes(constraints("미리보기 사진은 1~10개만 가능합니다."), defaultValue(5)),
					parameterWithName("page").description("페이지")
						.optional().attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수")
						.optional().attributes(defaultValue(10))
				),
				responseFields(
					fieldWithPath("centerAddress").type(JsonFieldType.STRING).description("중심 좌표의 지면 주소"),
					fieldWithPath("centerRoadAddress").type(JsonFieldType.STRING).description("중심 좌표의 도로면 주소"),
					fieldWithPath("recommendedSpots").type(JsonFieldType.ARRAY).description("주변 추천 스팟 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("recommendedSpots[].id").type(JsonFieldType.NUMBER).description("스팟 ID"),
					fieldWithPath("recommendedSpots[].address").type(JsonFieldType.STRING).description("스팟 주소"),
					fieldWithPath("recommendedSpots[].postCount").type(JsonFieldType.NUMBER).description("스팟 방명록수"),
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
		//give
		given(spotService.getNearbySpotList(any(NearbySpotRequest.class))).willReturn(new NearbySpotListResponse(
			List.of(new NearbySpotResponse(1L, DEFAULT_COORD))));
		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lat", String.valueOf(DEFAULT_COORD.lat()))
				.queryParam("lon", String.valueOf(DEFAULT_COORD.lon()))
				.queryParam("radius", "5000"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("lat").description("위도").attributes(coordConstraints()),
					parameterWithName("lon").description("경도").attributes(coordConstraints()),
					parameterWithName("radius").description("반경").attributes(constraints("반경(m)은 0보다 커야 됩니다."))
				),
				responseFields(
					fieldWithPath("spots[].id").type(JsonFieldType.NUMBER).description("스팟 id"),
					fieldWithPath("spots[].coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("spots[].coord.lon").type(JsonFieldType.NUMBER).description("경도")
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
			.coord(DEFAULT_COORD)
			.postPreviewResponses(List.of(
				new PostPreviewResponse(3L, 1L, "http://image3.com")))
			.build();
	}
}
