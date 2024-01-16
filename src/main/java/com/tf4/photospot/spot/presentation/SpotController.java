package com.tf4.photospot.spot.presentation;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.argument.AuthUserId;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchByCoordResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.application.response.SpotResponse;
import com.tf4.photospot.spot.presentation.response.RecommendedSpotHttpResponse;
import com.tf4.photospot.spot.presentation.response.RecommendedSpotListHttpResponse;
import com.tf4.photospot.spot.presentation.response.SpotHttpResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class SpotController {

	private final SpotService spotService;
	private final MapService mapService;

	@GetMapping("/spots/recommended")
	public ApiResponse<RecommendedSpotListHttpResponse> getSpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") @Positive(message = "반경(m)은 0보다 커야 됩니다.") Integer radius,
		@RequestParam(name = "postPreviewCount", defaultValue = "5")
		@Range(min = 1, max = 10, message = "미리보기 사진은 1~10개만 가능합니다.") Integer postPreviewCount,
		Pageable pageable
	) {
		SearchByCoordResponse searchByCoordResponse = mapService.searchByCoord(PointConverter.convert(coord));
		RecommendedSpotListResponse recommendedSpotsResponse = spotService.getRecommendedSpotList(
			new RecommendedSpotsRequest(coord.toCoord(), radius, postPreviewCount, pageable));
		return ApiResponse.success(RecommendedSpotListHttpResponse.builder()
			.centerAddress(searchByCoordResponse.address())
			.centerRoadAddress(searchByCoordResponse.roadAddress())
			.recommendedSpots(RecommendedSpotHttpResponse.convert(recommendedSpotsResponse.recommendedSpots()))
			.hasNext(recommendedSpotsResponse.hasNext())
			.build());
	}

	@GetMapping("/spots")
	public ApiResponse<NearbySpotListResponse> getNearbySpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") @Positive(message = "반경(m)은 0보다 커야 됩니다.") Integer radius
	) {
		return ApiResponse.success(spotService.getNearbySpotList(new NearbySpotRequest(coord.toCoord(), radius)));
	}

	@GetMapping("/spots/{spotId}")
	public ApiResponse<SpotHttpResponse> getSpot(
		@PathVariable(name = "spotId") Long spotId,
		@ModelAttribute @Valid CoordinateDto startingCoord,
		@RequestParam(name = "postPreviewCount", defaultValue = "5")
		@Range(min = 1, max = 10, message = "미리보기 사진은 1~10개만 가능합니다.") Integer postPreviewCount,
		//프론트 요청으로 외부 API가 결합되어 있어서 예외 상황시 호출하지 않도록 플래그 추가
		@RequestParam(name = "distance", defaultValue = "true") Boolean requireDistance,
		@AuthUserId Long userId
	) {
		Integer distance = 0;
		SpotResponse spotResponse = spotService.findSpot(spotId, userId, postPreviewCount);
		if (requireDistance) {
			distance = mapService.searchDistanceBetween(startingCoord.toCoord(), spotResponse.coord());
		}
		return ApiResponse.success(SpotHttpResponse.of(distance, spotResponse));
	}
}
