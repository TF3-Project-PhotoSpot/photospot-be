package com.tf4.photospot.spot.presentation;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
import com.tf4.photospot.spot.presentation.response.RecommendedSpotsHttpResponse;

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

	@GetMapping("/spots/recommended")
	public ResponseEntity<ApiResponse<RecommendedSpotsHttpResponse>> getSpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") @Positive(message = "반경(m)은 0보다 커야 됩니다.") Integer radius,
		@RequestParam(name = "postThumbnailCount", defaultValue = "5")
		@Range(min = 1L, max = 10L, message = "미리보기 사진은 1~10개만 가능합니다.") Integer postThumbnailCount,
		Pageable pageable

	) {
		RecommendedSpotsResponse recommendedSpotsResponse = spotService.getRecommendedSpotList(
			new RecommendedSpotsRequest(coord.toCoord(), radius, postThumbnailCount, pageable));
		return ResponseEntity.ok(ApiResponse.success(RecommendedSpotsHttpResponse.of(
			"test address", recommendedSpotsResponse)));
	}

	@GetMapping("/spot")
	public ResponseEntity<ApiResponse<FindSpotResponse>> getSpot(
		@ModelAttribute @Valid CoordinateDto coord
	) {
		var response = spotService.findSpot(new FindSpotRequest(coord.toCoord()));
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
