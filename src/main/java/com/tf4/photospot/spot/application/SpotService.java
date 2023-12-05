package com.tf4.photospot.spot.application;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.post.application.response.PostThumbnailsResponse;
import com.tf4.photospot.post.presentation.PostJdbcRepository;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
import com.tf4.photospot.spot.domain.MapApiClient;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.spot.infrastructure.SpotSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpotService {
	private final MapApiClient mapApiClient;
	private final SpotRepository spotRepository;
	private final SpotSearchRepository spotSearchRepository;
	private final PostJdbcRepository postJdbcRepository;

	/*
	 *	특정 좌표의 반경 내 추천 스팟들의 최신 방명록 미리보기를 조회합니다.
	 * 	추천 스팟은 방명록이 많은 순으로 정렬 됩니다.
	 * */
	public RecommendedSpotsResponse getRecommendedSpotList(RecommendedSpotsRequest request) {
		Slice<Spot> recommendedSpots = spotSearchRepository.searchRecommendedSpots(request.coord(),
			request.radius(), request.pageable());
		if (recommendedSpots.isEmpty()) {
			return RecommendedSpotsResponse.emptyResponse();
		}
		PostThumbnailsResponse postThumbnailsResponse = postJdbcRepository.findRecentlyPostThumbnailsInSpotIds(
			recommendedSpots.stream().map(Spot::getId).toList(), request.postThumbnailCount());
		return RecommendedSpotsResponse.builder()
			.recommendedSpots(recommendedSpots.stream()
				.map(spot -> RecommendedSpotResponse.of(spot, postThumbnailsResponse.getPostThumbnails(spot.getId())))
				.toList())
			.hasNext(recommendedSpots.hasNext())
			.build();
	}

	public FindSpotResponse findSpot(FindSpotRequest request) {
		String address = mapApiClient.findAddressByCoordinate(request.coord())
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_ADDRESS_FOR_GIVEN_COORD));
		Point foundCoord = mapApiClient.findCoordinateByAddress(address)
			.orElseThrow(() -> new ApiException(MapErrorCode.NO_COORD_FOR_GIVEN_ADDRESS));

		return spotRepository.findByCoord(foundCoord)
			.map(FindSpotResponse::toSpotResponse)
			.orElseGet(() -> FindSpotResponse.toNonSpotResponse(foundCoord, address));
	}
}
