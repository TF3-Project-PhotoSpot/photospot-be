package com.tf4.photospot.spot.application;

import static com.tf4.photospot.global.util.PointConverter.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.domain.MapApiClient;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;

@Transactional
@SpringBootTest
class SpotServiceTest {
	@Autowired
	private SpotService spotService;

	@Autowired
	private SpotRepository spotRepository;

	@MockBean
	private MapApiClient mapApiClient;

	@Nested
	@DisplayName("특정 좌표에 포함되는 스팟 조회")
	class FindSpot {

		@DisplayName("등록된 스팟이 없으면 해당 장소의 정보를 반환한다")
		@Test
		void findUnregisteredSpot() throws Exception {
			var coord = convert(127.046817765572, 37.6676198504815);
			var spot = new Spot("서울시 마들로 646", coord, 0L);

			given(mapApiClient.findAddressByCoordinate(any(Point.class)))
				.willReturn(Optional.of("서울시 마들로 646"));
			given(mapApiClient.findCoordinateByAddress(anyString()))
				.willReturn(Optional.of(coord));

			//when
			var foundSpot = spotService.findSpot(new FindSpotRequest(coord));

			//then
			assertThat(foundSpot.isSpot()).isFalse();
			assertThat(foundSpot.id()).isNull();
			assertThat(foundSpot.address()).isEqualTo("서울시 마들로 646");
			assertThat(foundSpot.coord().lon()).isEqualTo(127.046817765572);
			assertThat(foundSpot.coord().lat()).isEqualTo(37.6676198504815);
		}

		@DisplayName("등록된 스팟이 있으면 해당 스팟의 정보를 반환한다.")
		@Test
		void findRegisteredSpot() throws Exception {
			//given
			var coord = convert(127.046817765572, 37.6676198504815);
			var spot = new Spot("서울시 마들로 646", coord, 0L);
			spotRepository.save(spot);

			given(mapApiClient.findAddressByCoordinate(any(Point.class)))
				.willReturn(Optional.of("서울시 마들로 646"));
			given(mapApiClient.findCoordinateByAddress(anyString()))
				.willReturn(Optional.of(coord));

			//when
			var foundSpot = spotService.findSpot(new FindSpotRequest(coord));

			//then
			assertThat(foundSpot.isSpot()).isTrue();
			assertThat(foundSpot.id()).isNotNull();
			assertThat(foundSpot.address()).isEqualTo("서울시 마들로 646");
			assertThat(foundSpot.coord().lon()).isEqualTo(127.046817765572);
			assertThat(foundSpot.coord().lat()).isEqualTo(37.6676198504815);
		}

		@DisplayName("해당 좌표에 해당하는 장소를 지도에서 찾을 수 없으면 NO_ADDRESS_FOR_GIVEN_COORD 예외가 발생한다.")
		@Test
		void notFoundForGivenCoord() throws Exception {
			//given
			var coord = convert(127.046817765572, 37.6676198504815);
			given(mapApiClient.findAddressByCoordinate(any(Point.class)))
				.willReturn(Optional.empty());

			//when then
			assertThatThrownBy(() -> spotService.findSpot(new FindSpotRequest(coord)))
				.isInstanceOf(ApiException.class)
				.extracting("statusCode", "message")
				.containsExactly(
					MapErrorCode.NO_ADDRESS_FOR_GIVEN_COORD.getStatusCode(),
					MapErrorCode.NO_ADDRESS_FOR_GIVEN_COORD.getMessage()
				);
		}

		@DisplayName("해당 주소에 해당하는 장소의 좌표를 찾을 수 없으면 NO_COORD_FOR_GIVEN_ADDRESS 예외가 발생한다.")
		@Test
		void notFoundForGivenAddress() throws Exception {
			//given
			var coord = convert(127.046817765572, 37.6676198504815);
			var address = "서울시 도봉구 마들로 646";
			given(mapApiClient.findAddressByCoordinate(any(Point.class))).willReturn(Optional.of(address));
			given(mapApiClient.findCoordinateByAddress(anyString()))
				.willReturn(Optional.empty());

			//when then
			assertThatThrownBy(() -> spotService.findSpot(new FindSpotRequest(coord)))
				.isInstanceOf(ApiException.class)
				.extracting("statusCode", "message")
				.containsExactly(
					MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS.getStatusCode(),
					MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS.getMessage()
				);
		}
	}
}