package com.tf4.photospot.spot.presentation;

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
import com.tf4.photospot.spot.application.request.NearbySpotRequest;
import com.tf4.photospot.spot.application.request.RecommendedSpotsRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.NearbySpotListResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
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
	public ResponseEntity<ApiResponse<RecommendedSpotsResponse>> getSpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") Long radius
	) {
		var response = spotService.getRecommendedSpotList(new RecommendedSpotsRequest(coord.toCoord(), radius));
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/spot")
	public ResponseEntity<ApiResponse<FindSpotResponse>> getSpot(
		@ModelAttribute @Valid CoordinateDto coord
	) {
		var response = spotService.findSpot(new FindSpotRequest(coord.toCoord()));
		return ResponseEntity.ok(ApiResponse.success(response));

	}

	@GetMapping("/spots")
	public ResponseEntity<ApiResponse<NearbySpotListResponse>> getNearbySpotList(
		@ModelAttribute @Valid CoordinateDto coord,
		@RequestParam(name = "radius") @PositiveOrZero(message = "반경(m)은 0보다 커야 됩니다.") Integer radius
	) {
		var response = spotService.getNearbySpotList(new NearbySpotRequest(coord.toCoord(), radius));
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
