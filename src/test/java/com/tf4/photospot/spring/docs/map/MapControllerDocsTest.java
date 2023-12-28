package com.tf4.photospot.spring.docs.map;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
		given(mapService.searchByCoord(any(Point.class))).willReturn(createSearchByCoordResponse());
		given(mapService.searchByAddress(anyString(), anyString())).willReturn(createSearchByAddressResponse());
		//when then
		mockMvc.perform(get("/api/v1/map/search/location")
				.queryParam("lat", "35.97")
				.queryParam("lon", "126.99"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("lat").description("위도").attributes(coordConstraints()),
					parameterWithName("lon").description("경도").attributes(coordConstraints())
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("address").type(JsonFieldType.STRING).description("지번 주소"),
					fieldWithPath("addressCoord").type(JsonFieldType.OBJECT).description("지번 주소 좌표"),
					fieldWithPath("addressCoord.lat").type(JsonFieldType.NUMBER).description("지번 주소 위도"),
					fieldWithPath("addressCoord.lon").type(JsonFieldType.NUMBER).description("지번 주소 경도"),
					fieldWithPath("roadAddress").type(JsonFieldType.STRING).description("도로명 주소"),
					fieldWithPath("roadAddressCoord").type(JsonFieldType.OBJECT).description("도로명 주소 좌표"),
					fieldWithPath("roadAddressCoord.lat").type(JsonFieldType.NUMBER).description("도로명 주소 위도"),
					fieldWithPath("roadAddressCoord.lon").type(JsonFieldType.NUMBER).description("도로명 주소 경도")
				)));
	}

	private static SearchByCoordResponse createSearchByCoordResponse() {
		return SearchByCoordResponse.builder()
			.address("전북 익산시 부송동 100")
			.roadAddress("전북 익산시 망산길 11-17")
			.build();
	}

	private static SearchByAddressResponse createSearchByAddressResponse() {
		return SearchByAddressResponse.builder()
			.address("전북 익산시 부송동 100")
			.addressCoord(new CoordinateDto(126.99, 35.97))
			.roadAddress("전북 익산시 망산길 11-17")
			.roadAddressCoord(new CoordinateDto(126.99, 35.97))
			.build();
	}
}
