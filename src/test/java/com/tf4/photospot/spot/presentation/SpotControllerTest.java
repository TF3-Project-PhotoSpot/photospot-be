package com.tf4.photospot.spot.presentation;

import static java.util.Collections.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;

@WebMvcTest(controllers = SpotController.class)
class SpotControllerTest {
	@MockBean
	private SpotService spotService;

	@Autowired
	private MockMvc mockMvc;

	@DisplayName("주변 스팟 장소를 조회한다.")
	@Test
	void getNearbySpots() throws Exception {
		//given
		NearbySpotListResponse response = new NearbySpotListResponse(emptyList());
		given(spotService.getNearbySpotList(any(NearbySpotRequest.class))).willReturn(response);

		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lon", "127.0")
				.queryParam("lat", "37.0")
				.queryParam("radius", "5000")
			)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").isNotEmpty())
			.andExpect(jsonPath("$.message").isNotEmpty())
			.andExpect(jsonPath("$.data.spots").isArray());
	}

	@DisplayName("주변 스팟 조회 반경은 0보다 큰 수만 가능하다.")
	@ValueSource(strings = {"-1", "0"})
	@ParameterizedTest(name = "radius = {0}")
	void getNearbySpotsFailToNonPasitiveRadius(String radius) throws Exception {
		//when then
		mockMvc.perform(get("/api/v1/spots")
				.queryParam("lon", "127.0")
				.queryParam("lat", "37.0")
				.queryParam("radius", radius)
			)
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
			.andExpect(jsonPath("$.message").value("Invalid Parameter"))
			.andExpect(jsonPath("$.errors[0].value").value(radius))
			.andExpect(jsonPath("$.errors[0].message").value("반경(m)은 0보다 커야 됩니다."));
	}
}
