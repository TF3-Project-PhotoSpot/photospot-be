package com.tf4.photospot.spot.application;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.infrastructure.PostJdbcRepository;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.NearbySpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.spot.infrastructure.SpotSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpotService {
	private final SpotRepository spotRepository;
	private final SpotSearchRepository spotSearchRepository;
	private final PostJdbcRepository postJdbcRepository;

	/*
	 *	특정 좌표의 반경 내 추천 스팟들의 최신 방명록 미리보기를 조회합니다.
	 * 	추천 스팟은 방명록이 많은 순으로 정렬 됩니다.
	 * */
	public RecommendedSpotListResponse getRecommendedSpotList(RecommendedSpotsRequest request) {
		Slice<Spot> recommendedSpots = spotSearchRepository.searchRecommendedSpots(request.coord(),
			request.radius(), request.pageable());
		if (recommendedSpots.isEmpty()) {
			return RecommendedSpotListResponse.emptyResponse();
		}
		List<PostPreviewResponse> postThumbnailsResponse = postJdbcRepository.findRecentlyPostThumbnailsInSpotIds(
			recommendedSpots.stream().map(Spot::getId).toList(), request.postPreviewCount());
		return RecommendedSpotListResponse.of(recommendedSpots, postThumbnailsResponse);
	}

	public NearbySpotListResponse getNearbySpotList(NearbySpotRequest request) {
		List<NearbySpotResponse> nearbySpots = spotRepository.findNearbySpots(request.coord(), request.radius());
		return new NearbySpotListResponse(nearbySpots);
	}
}
