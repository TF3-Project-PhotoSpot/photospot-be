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
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.presentation.MapController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class MapControllerDocsTest extends RestDocsSupport {
	private final MapService mapService = mock(MapService.class);

	@Override
	protected Object initController() {
		return new MapController(mapService);
	}

	@DisplayName("특정 좌표로 지도의 장소를 찾는다.")
	@Test
	void findRegisteredSpot() throws Exception {
		//given
		Double lat = 37.6676198504815;
		Double lon = 127.046817765572;
		Point exactCoord = PointConverter.convert(new CoordinateDto(lon, lat));

		given(mapService.searchAddress(any(Point.class))).willReturn("서울특별시 도봉구 마들로 646");
		given(mapService.searchCoordinate(anyString())).willReturn(exactCoord);
		//when then
		mockMvc.perform(get("/api/v1/map/search/location")
				.queryParam("lat", String.valueOf(lat))
				.queryParam("lon", String.valueOf(lon)))
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
					fieldWithPath("data.address").type(JsonFieldType.STRING).description("장소 주소"),
					fieldWithPath("data.coord").type(JsonFieldType.OBJECT).description("장소의 정확한 좌표"),
					fieldWithPath("data.coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("data.coord.lon").type(JsonFieldType.NUMBER).description("경도")
				)));
	}
}
