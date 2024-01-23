package com.tf4.photospot.spot.application;

import static com.tf4.photospot.global.util.PointConverter.*;
import static com.tf4.photospot.support.TestFixture.*;
import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.application.response.SpotResponse;
import com.tf4.photospot.spot.domain.BookmarFolderRepository;
import com.tf4.photospot.spot.domain.BookmarkFolder;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotBookmark;
import com.tf4.photospot.spot.domain.SpotBookmarkRepository;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

@Transactional
class SpotServiceTest extends IntegrationTestSupport {
	@Autowired
	private SpotService spotService;

	@Autowired
	private SpotRepository spotRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SpotBookmarkRepository spotBookmarkRepository;

	@Autowired
	private BookmarFolderRepository bookmarFolderRepository;

	@DisplayName("특정 스팟을 조회한다.")
	@TestFactory
	Stream<DynamicTest> getSpot() {
		//given
		Point coord = convert(127.0, 37.0);
		Spot spot = createSpot("address", coord, 5L);
		spotRepository.save(spot);
		User user = createUser("이성빈");
		userRepository.save(user);

		return Stream.of(
			dynamicTest("방명록이 없는 스팟을 조회한다.", () -> {
				//when
				SpotResponse response = spotService.findSpot(spot.getId(), user.getId(), 5);
				//then
				assertThat(response.id()).isEqualTo(spot.getId());
				assertThat(response.address()).isEqualTo("address");
				assertThatObject(response.coord()).isEqualTo(coord);
				assertThat(response.postCount()).isEqualTo(5);
				assertThat(response.bookmarked()).isFalse();
				assertThat(response.previewResponses()).isEmpty();
			}),
			dynamicTest("북마크 등록 여부를 알려준다.", () -> {
				//given
				BookmarkFolder defaultBookmark = BookmarkFolder.createDefaultBookmark(user);
				bookmarFolderRepository.save(defaultBookmark);
				SpotBookmark spotBookmark = createSpotBookmark(user, spot, defaultBookmark);
				spotBookmarkRepository.save(spotBookmark);
				//when
				SpotResponse response = spotService.findSpot(spot.getId(), user.getId(), 5);
				//then
				assertThat(response.bookmarked()).isTrue();
			}),
			dynamicTest("북마크 등록 여부를 알려준다.", () -> {
				//given
				BookmarkFolder defaultBookmark = BookmarkFolder.createDefaultBookmark(user);
				bookmarFolderRepository.save(defaultBookmark);
				SpotBookmark spotBookmark = createSpotBookmark(user, spot, defaultBookmark);
				spotBookmarkRepository.save(spotBookmark);
				//when
				SpotResponse response = spotService.findSpot(spot.getId(), user.getId(), 5);
				//then
				assertThat(response.bookmarked()).isTrue();
			}),
			dynamicTest("최신 방명록 미리보기를 조회한다.", () -> {
				//given
				List<Post> posts = createList(() -> createPost(spot, user, createPhoto("photoUrl"), createPoint()),
					5);
				postRepository.saveAll(posts);
				//when
				SpotResponse response = spotService.findSpot(spot.getId(), user.getId(), 5);
				//then
				assertThat(response.previewResponses())
					.isNotEmpty()
					.isSortedAccordingTo(comparingLong(PostPreviewResponse::postId).reversed());
			})
		);
	}

	@DisplayName("주변 추천 스팟을 조회한다.")
	@TestFactory
	Stream<DynamicTest> recommendedSpot() {
		//given
		RecommendedSpotsRequest request = new RecommendedSpotsRequest(createPoint(), 5000,
			5, PageRequest.of(0, 10));
		User user = createUser("이성빈");
		Spot spotWithMostPosts = createSpot("주소1", createPoint(), 15L);
		Spot spotWithMiddlePosts = createSpot("주소2", createPoint(), 10L);
		Spot spotWithLeastPosts = createSpot("주소3", createPoint(), 5L);
		userRepository.save(user);
		return Stream.of(
			dynamicTest("반경 내 추천 스팟이 없으면 빈 리스트가 반환이 된다.", () -> {
				//when
				RecommendedSpotListResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots()).isEmpty();
			}),
			dynamicTest("스팟에 비공개 처리 된 방명록은 제외 된다.", () -> {
				//given
				spotRepository.saveAll(List.of(spotWithMostPosts, spotWithMiddlePosts, spotWithLeastPosts));
				postRepository.save(createPost(spotWithMostPosts, user, createPhoto(), createPoint(), true));
				postRepository.save(createPost(spotWithMiddlePosts, user, createPhoto(), createPoint(), false));
				//when
				RecommendedSpotListResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots().get(0).postPreviewResponses()).isEmpty();
				assertThat(response.recommendedSpots().get(1).postPreviewResponses()).isNotEmpty();
			}),
			dynamicTest("스팟은 방명록 개수가 많은 순서로 정렬이 된다.", () -> {
				//given
				postRepository.saveAll(Stream.of(
					createList(() -> createPost(spotWithMostPosts, user, createPhoto(), createPoint()), 15),
					createList(() -> createPost(spotWithMiddlePosts, user, createPhoto(), createPoint()), 10),
					createList(() -> createPost(spotWithLeastPosts, user, createPhoto(), createPoint()), 15)
				).flatMap(Collection::stream).toList());
				//when
				RecommendedSpotListResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots())
					.isSortedAccordingTo(comparingLong(RecommendedSpotResponse::postCount).reversed());
			}),
			dynamicTest("각 스팟의 방명록들은 최신순으로 정렬이 된다.", () -> {
				//when
				RecommendedSpotListResponse response = spotService.getRecommendedSpotList(request);
				//then
				assertThat(response.recommendedSpots()).isNotEmpty().allSatisfy(recommendedSpot ->
					assertThat(recommendedSpot.postPreviewResponses())
						.isSortedAccordingTo(comparingLong(PostPreviewResponse::postId).reversed())
				);
			}),
			dynamicTest("다음 추천 스팟 목록이 있으면 hasNext = true를 반환한다", () -> {
				//given when
				RecommendedSpotListResponse response = spotService.getRecommendedSpotList(
					new RecommendedSpotsRequest(createPoint(), 5000,
						5, PageRequest.of(0, 2)));
				//then
				assertThat(response.hasNext()).isTrue();
			}),
			dynamicTest("다음 추천 스팟 목록이 없으면 hasNext = false를 반환한다", () -> {
				//given when
				RecommendedSpotListResponse response = spotService.getRecommendedSpotList(
					new RecommendedSpotsRequest(createPoint(), 1,
						5, PageRequest.of(0, 100)));
				//then
				assertThat(response.hasNext()).isFalse();
			})
		);
	}

	@DisplayName("반경 내에 위치한 주변 스팟 조회")
	@Nested
	class NearbySpotTest {
		@DisplayName("중심에서 직선 거리 반경 이내에 있는 장소만 조회한다.")
		@Test
		void getNearbySpotWithinRadius() {
			//given
			int radius = 5000; // 반경 5km
			Point centerCoord = convert(127.0468177, 37.6676198);
			Spot centerSpot = createSpot("서울시 도봉구 마들로 646", centerCoord, 1L);
			Spot innerSpot = createSpot("중심에서 도보 3.8km", convert(127.0273281, 37.6400187), 1L);
			Spot outerSpot = createSpot("중심에서 도보 5.8km", convert(127.0467641, 37.7155734), 1L);
			spotRepository.saveAll(List.of(centerSpot, innerSpot, outerSpot));

			//when
			NearbySpotListResponse response = spotService.getNearbySpotList(new NearbySpotRequest(centerCoord, radius));

			//then
			assertThat(response.spots()).hasSize(2);
			assertThat(response.spots())
				.extracting("id")
				.containsExactlyInAnyOrder(centerSpot.getId(), innerSpot.getId());
		}

		@DisplayName("중심에서 직선 거리 이내에 장소가 없을 경우 빈 List가 반환된다")
		@Test
		void getNearbySpotEmptyList() {
			//given
			int radius = 1;
			Spot outerSpot = createSpot("주소", createPoint(), 1L);
			spotRepository.save(outerSpot);

			//when
			NearbySpotListResponse response = spotService.getNearbySpotList(
				new NearbySpotRequest(createPoint(), radius));

			//then
			assertThat(response.spots()).isEmpty();
		}
	}
}
