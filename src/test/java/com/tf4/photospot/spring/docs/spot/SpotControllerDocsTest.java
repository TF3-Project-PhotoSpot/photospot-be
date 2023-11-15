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
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
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
		var recommendedSpots = List.of(RecommendedSpotResponse.builder()
				.id(1L)
				.address("서울시 도봉구 마들로 643")
				.postCount(10L)
				.coord(new CoordinateDto(32.0000, 70.0000))
				.photoUrls(List.of(
					"http://aaaaaa1.com",
					"http://aaaaaa2.com",
					"http://aaaaaa3.com"
				)).build(),
			RecommendedSpotResponse.builder()
				.id(2L)
				.address("서울시 도봉구 마들로 645")
				.postCount(15L)
				.coord(new CoordinateDto(35.0000, 65.0000))
				.photoUrls(List.of(
					"http://aaaaaa4.com",
					"http://aaaaaa5.com",
					"http://aaaaaa6.com"
				)).build()
		);

		given(spotService.getRecommendedSpotList(any(RecommendedSpotsRequest.class)))
			.willReturn(RecommendedSpotsResponse.builder()
				.centerAddress("서울시 도봉구 마들로 646")
				.recommendedSpots(recommendedSpots)
				.build());
		//when
		mockMvc.perform(get("/api/v1/spots/recommended")
				.queryParam("lat", "35.0000")
				.queryParam("lon", "70.0000")
				.queryParam("radius", "200")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("recommended-spot-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("lat").description("위도(latitude)"),
					parameterWithName("lon").description("경도(longitude)"),
					parameterWithName("radius").description("반경(단위 m)")
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
						.description("최신 방명록 사진")
				)));
	}
}
