package com.tf4.photospot.spring.docs.map;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchByAddressResponse;
import com.tf4.photospot.map.application.response.SearchByCoordResponse;
import com.tf4.photospot.map.presentation.MapController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

class MapControllerDocsTest extends RestDocsSupport {
	private final MapService mapService = mock(MapService.class);

	@Override
	protected Object initController() {
		return new MapController(mapService);
	}

	@DisplayName("특정 좌표로 지도의 장소를 찾는다.")
	@Test
	void findRegisteredSpot() throws Exception {
		//given
		String lat = "35.97664845766847";
		String lon = "126.99597295767953";
		SearchByCoordResponse searchByCoordResponse = SearchByCoordResponse.builder()
			.address("전북 익산시 부송동 100")
			.roadAddress("전북 익산시 망산길 11-17")
			.build();
		given(mapService.searchByCoord(any(Point.class))).willReturn(searchByCoordResponse);
		given(mapService.searchByAddress(anyString(), anyString())).willReturn(SearchByAddressResponse.builder()
			.address("전북 익산시 부송동 100")
			.addressCoord(new CoordinateDto(126.99597295767953, 35.97664845766847))
			.roadAddress("전북 익산시 망산길 11-17")
			.roadAddressCoord(new CoordinateDto(126.99599512792346, 35.976749396987046))
			.build());
		//when then
		mockMvc.perform(get("/api/v1/map/search/location")
				.queryParam("lat", lat)
				.queryParam("lon", lon))
			.andDo(print())
			.andExpect(status().isOk())
			.andDo(document("search-location",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				queryParameters(
					parameterWithName("lat").description("위도(latitude)"),
					parameterWithName("lon").description("경도(longitude)")
				),
				responseFields(
					fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
					fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
					fieldWithPath("data.address").type(JsonFieldType.STRING).description("지번 주소"),
					fieldWithPath("data.addressCoord").type(JsonFieldType.OBJECT).description("지번 주소 좌표"),
					fieldWithPath("data.addressCoord.lat").type(JsonFieldType.NUMBER).description("지번 주소 위도"),
					fieldWithPath("data.addressCoord.lon").type(JsonFieldType.NUMBER).description("지번 주소 경도"),
					fieldWithPath("data.roadAddress").type(JsonFieldType.STRING).description("도로명 주소"),
					fieldWithPath("data.roadAddressCoord").type(JsonFieldType.OBJECT).description("도로명 주소 좌표"),
					fieldWithPath("data.roadAddressCoord.lat").type(JsonFieldType.NUMBER).description("도로명 주소 위도"),
					fieldWithPath("data.roadAddressCoord.lon").type(JsonFieldType.NUMBER).description("도로명 주소 경도")
				)));
	}
}
