package com.tf4.photospot.spring.docs.spot;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.NearbySpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.presentation.SpotController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class SpotControllerDocsTest extends RestDocsSupport {
	private final SpotService spotService = mock(SpotService.class);

	@Override
	protected Object initController() {
		return new SpotController(spotService);
	}

	@DisplayName("주변 추천 스팟 리스트를 조회한다.")
	@Test
	void getRecommendedSpotList() throws Exception {
		//given
		Double lat = 37.6676198504815;
		Double lon = 127.046817765572;
		var recommendedSpots = List.of(RecommendedSpotResponse.builder()
				.id(1L)
				.address("서울시 도봉구 마들로 643")
				.postCount(10L)
				.coord(new CoordinateDto(32.0000, 70.0000))
				.postPreviewResponses(List.of(
					new PostPreviewResponse(3L, 1L, "http://aaaaaa3.com"),
					new PostPreviewResponse(2L, 2L, "http://aaaaaa2.com"),
					new PostPreviewResponse(1L, 3L, "http://aaaaaa1.com")
				)).build(),
			RecommendedSpotResponse.builder()
				.id(2L)
				.address("서울시 도봉구 마들로 645")
				.postCount(15L)
				.coord(new CoordinateDto(35.0000, 65.0000))
				.postPreviewResponses(List.of(
					new PostPreviewResponse(6L, 2L, "http://aaaaaa6.com"),
					new PostPreviewResponse(5L, 2L, "http://aaaaaa5.com"),
					new PostPreviewResponse(4L, 2L, "http://aaaaaa4.com")
				)).build()
		);
		RecommendedSpotListResponse recommendedSpotsResponse = RecommendedSpotListResponse.builder()
			.recommendedSpots(recommendedSpots)
			.hasNext(true)
			.build();

		given(spotService.getRecommendedSpotList(any(RecommendedSpotsRequest.class)))
			.willReturn(recommendedSpotsResponse);
		//when then
		mockMvc.perform(get("/api/v1/spots/recommended")
				.queryParam("lat", String.valueOf(lat))
				.queryParam("lon", String.valueOf(lon))
				.queryParam("radius", "200")
				.queryParam("page", "0")
				.queryParam("size", "10")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("recommended-spot-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("lat").description("위도(latitude)"),
					parameterWithName("lon").description("경도(longitude)"),
					parameterWithName("radius").description("반경(단위 m)"),
					parameterWithName("page").description("페이지(0부터 시작)"),
					parameterWithName("size").description("페이지당 개수")
				),
				responseFields(
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
					fieldWithPath("data.centerAddress").type(JsonFieldType.STRING).description("중심 좌표"),
					fieldWithPath("data.recommendedSpots").type(JsonFieldType.ARRAY).description("주변 추천 스팟 리스트"),
					fieldWithPath("data.recommendedSpots[].id").type(JsonFieldType.NUMBER).description("추천 스팟 ID"),
					fieldWithPath("data.recommendedSpots[].address").type(JsonFieldType.STRING).description("추천 스팟 주소"),
					fieldWithPath("data.recommendedSpots[].postCount").type(JsonFieldType.NUMBER)
						.description("추천 장소 방명록수"),
					fieldWithPath("data.recommendedSpots[].coord").type(JsonFieldType.OBJECT).description("스팟 좌표"),
					fieldWithPath("data.recommendedSpots[].coord.lat").type(JsonFieldType.NUMBER).description("스팟 좌표"),
					fieldWithPath("data.recommendedSpots[].coord.lon").type(JsonFieldType.NUMBER).description("스팟 좌표"),
					fieldWithPath("data.recommendedSpots[].photoUrls").type(JsonFieldType.ARRAY)
						.description("최신 방명록 사진"),
					fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부")
				)));
	}

	@DisplayName("특정 좌표로 등록된 스팟을 찾는다.")
	@Test
	void findRegisteredSpot() throws Exception {
		//given
		Double lat = 37.6676198504815;
		Double lon = 127.046817765572;

		given(spotService.findSpot(any(FindSpotRequest.class)))
			.willReturn(new FindSpotResponse(Boolean.TRUE, 1L, "서울특별시 도봉구 마들로 646", new CoordinateDto(lat, lon)));
		//when then
		mockMvc.perform(get("/api/v1/spot")
				.queryParam("lat", String.valueOf(lat))
				.queryParam("lon", String.valueOf(lon)))
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("find-registered-spot",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("lat").description("위도(latitude)"),
					parameterWithName("lon").description("경도(longitude)")
				),
				responseFields(
					fieldWithPath("data.isSpot").type(JsonFieldType.BOOLEAN).description("스팟 등록 여부"),
					fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("스팟 id"),
					fieldWithPath("data.address").type(JsonFieldType.STRING).description("스팟 주소"),
					fieldWithPath("data.coord").type(JsonFieldType.OBJECT).description("스팟 좌표 정보"),
					fieldWithPath("data.coord.lat").type(JsonFieldType.NUMBER).description("스팟 위도"),
					fieldWithPath("data.coord.lon").type(JsonFieldType.NUMBER).description("스팟 경도")
				)));
	}

	@DisplayName("특정 좌표로 등록되지 않은 스팟을 찾는다.")
	@Test
	void findUnregisteredSpot() throws Exception {
		// given
		Double lat = 37.6676198504815;
		Double lon = 127.046817765572;
		var coord = PointConverter.convert(lat, lon);

		given(spotService.findSpot(any(FindSpotRequest.class)))
			.willReturn(FindSpotResponse.toNonSpotResponse(coord, "서울특별시 도봉구 마들로 646"));

		//when then
		mockMvc.perform(get("/api/v1/spot")
				.queryParam("lat", String.valueOf(lat))
				.queryParam("lon", String.valueOf(lon)))
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("find-unregistered-spot",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("lat").description("위도(latitude)"),
					parameterWithName("lon").description("경도(longitude)")
				),
				responseFields(
					fieldWithPath("data.isSpot").type(JsonFieldType.BOOLEAN).description("스팟 등록 여부"),
					fieldWithPath("data.id").type(JsonFieldType.NULL).description("스팟 id"),
					fieldWithPath("data.address").type(JsonFieldType.STRING).description("스팟 주소"),
					fieldWithPath("data.coord").type(JsonFieldType.OBJECT).description("스팟 좌표 정보"),
					fieldWithPath("data.coord.lat").type(JsonFieldType.NUMBER).description("스팟 위도"),
					fieldWithPath("data.coord.lon").type(JsonFieldType.NUMBER).description("스팟 경도")
				))
			);
	}

	@DisplayName("반경 내에 위치한 주변 스팟을 조회한다.")
	@Test
	void getNearbySpots() throws Exception {
		//given
		NearbySpotListResponse response = new NearbySpotListResponse(List.of(
			new NearbySpotResponse(1L, new CoordinateDto(127.0468177, 37.6676198)),
			new NearbySpotResponse(2L, new CoordinateDto(127.0273281, 37.6400187))
		));
		given(spotService.getNearbySpotList(any(NearbySpotRequest.class))).willReturn(response);
		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lat", "37.6676198")
				.queryParam("lon", "127.0468177")
				.queryParam("radius", "5000"))
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("nearby-spots",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("lat").description("위도(latitude)"),
					parameterWithName("lon").description("경도(longitude)"),
					parameterWithName("radius").description("반경")
				),
				responseFields(
					fieldWithPath("data.spots").type(JsonFieldType.ARRAY).description("주변 스팟 리스트"),
					fieldWithPath("data.spots[].id").type(JsonFieldType.NUMBER).description("스팟 id"),
					fieldWithPath("data.spots[].coord").type(JsonFieldType.OBJECT).description("스팟 좌표 정보"),
					fieldWithPath("data.spots[].coord.lat").type(JsonFieldType.NUMBER).description("스팟 위도"),
					fieldWithPath("data.spots[].coord.lon").type(JsonFieldType.NUMBER).description("스팟 경도")
				))
			);
	}
}
