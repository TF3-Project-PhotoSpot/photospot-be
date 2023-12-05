package com.tf4.photospot.spot.application;

import static com.tf4.photospot.global.util.PointConverter.*;
import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.post.application.response.PostThumbnailResponse;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
import com.tf4.photospot.spot.domain.MapApiClient;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;

import jakarta.persistence.EntityManager;

@Transactional
@SpringBootTest
class SpotServiceTest {
	@Autowired
	private SpotService spotService;

	@Autowired
	private SpotRepository spotRepository;

	@Autowired
	private PostRepository postRepository;

	@MockBean
	private MapApiClient mapApiClient;

	@Autowired
	EntityManager em;

	@DisplayName("주변 추천 스팟을 조회한다.")
	@TestFactory
	Stream<DynamicTest> recommendedSpot() {
		//given
		RecommendedSpotsRequest request = new RecommendedSpotsRequest(convert(127.0468177, 37.6676198), 5000,
			5, PageRequest.of(0, 10));
		Spot spotWithMostPosts = createSpot(convert(127.0468177, 37.6676198), 15L);
		Spot spotWithMiddlePosts = createSpot(convert(127.0468176, 37.6676197), 10L);
		Spot spotWithLeastPosts = createSpot(convert(127.0468175, 37.6676196), 5L);

		return Stream.of(
			dynamicTest("반경 내 추천 스팟이 없으면 빈 리스트가 반환이 된다.", () -> {
				//when
				RecommendedSpotsResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots()).isEmpty();
			}),
			dynamicTest("스팟에 비공개 처리 된 방명록은 제외 된다.", () -> {
				//given
				spotRepository.saveAll(List.of(spotWithMostPosts, spotWithMiddlePosts, spotWithLeastPosts));
				postRepository.saveAll(createPosts(spotWithMostPosts, 1, true));
				postRepository.saveAll(createPosts(spotWithMiddlePosts, 1, false));
				//when
				RecommendedSpotsResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots().get(0).postThumbnailResponses()).isEmpty();
				assertThat(response.recommendedSpots().get(1).postThumbnailResponses()).isNotEmpty();
			}),
			dynamicTest("스팟은 방명록 개수가 많은 순서로 정렬이 된다.", () -> {
				//given
				postRepository.saveAll(createPosts(spotWithMostPosts, 15, false));
				postRepository.saveAll(createPosts(spotWithMiddlePosts, 10, false));
				postRepository.saveAll(createPosts(spotWithLeastPosts, 5, false));
				//when
				RecommendedSpotsResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots())
					.isSortedAccordingTo(comparingLong(RecommendedSpotResponse::postCount).reversed());
			}),
			dynamicTest("각 스팟의 방명록들은 최신순으로 정렬이 된다.", () -> {
				//when
				RecommendedSpotsResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots()).allSatisfy(recommendedSpot ->
					assertThat(recommendedSpot.postThumbnailResponses())
						.isSortedAccordingTo(comparingLong(PostThumbnailResponse::postId).reversed())
				);
			}),
			dynamicTest("다음 추천 스팟 목록이 있으면 hasNext = true를 반환한다", () -> {
				//given when
				RecommendedSpotsResponse response = spotService.getRecommendedSpotList(
					new RecommendedSpotsRequest(convert(127.0468177, 37.6676198), 5000,
						5, PageRequest.of(0, 2)));
				//then
				assertThat(response.hasNext()).isTrue();
			}),
			dynamicTest("다음 추천 스팟 목록이 없으면 hasNext = false를 반환한다", () -> {
				//given when
				RecommendedSpotsResponse response = spotService.getRecommendedSpotList(
					new RecommendedSpotsRequest(convert(127.0468177, 37.6676198), 5000,
						5, PageRequest.of(0, 100)));
				//then
				assertThat(response.hasNext()).isFalse();
			})
		);
	}

	private static Spot createSpot(Point coord, long postCount) {
		return new Spot("test address", coord, postCount);
	}

	private List<Post> createPosts(Spot spot, int count, boolean isPrivate) {
		List<Post> posts = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			posts.add(Post.builder()
				.photo(new Photo("testUrl" + i))
				.spot(spot)
				.isPrivate(isPrivate)
				.build());
		}
		return posts;
	}

	@Nested
	@DisplayName("특정 좌표에 포함되는 스팟 조회")
	class FindSpot {

		@DisplayName("등록된 스팟이 없으면 해당 장소의 정보를 반환한다")
		@Test
		void findUnregisteredSpot() {
			var coord = convert(127.046817765572, 37.6676198504815);

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
		void findRegisteredSpot() {
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
		void notFoundForGivenCoord() {
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
		void notFoundForGivenAddress() {
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
