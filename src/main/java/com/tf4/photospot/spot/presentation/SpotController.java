package com.tf4.photospot.spot.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.FindSpotRequest;
import com.tf4.photospot.spot.application.response.FindSpotResponse;
import com.tf4.photospot.spot.application.response.RecommendedSpotsResponse;
import com.tf4.photospot.spot.presentation.request.FindSpotHttpRequest;
import com.tf4.photospot.spot.presentation.request.RecommendedSpotsHttpRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class SpotController {

	private final SpotService spotService;

	@GetMapping("/spots/recommended")
	public ResponseEntity<ApiResponse<RecommendedSpotsResponse>> getSpotList(
		@ModelAttribute @Valid RecommendedSpotsHttpRequest request
	) {
		var response = spotService.getRecommendedSpotList(request.toServiceRequest());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/spot")
	public ResponseEntity<ApiResponse<FindSpotResponse>> getSpot(
		@ModelAttribute @Valid FindSpotHttpRequest request
	) {
		var response = spotService.findSpot(new FindSpotRequest(request.toCoord()));
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
