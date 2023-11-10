package com.tf4.photospot.spot.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.spot.application.SpotService;
import com.tf4.photospot.spot.application.request.RecommendedSpotListServiceRequest;
import com.tf4.photospot.spot.application.response.RecommendedSpotListResponse;
import com.tf4.photospot.spot.presentation.request.RecommendedSpotListRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/v1/spots")
@RestController
@RequiredArgsConstructor
public class SpotController {

	private final SpotService spotService;

	@GetMapping("/recommended")
	public ResponseEntity<ApiResponse<RecommendedSpotListResponse>> getSpotList(
		@ModelAttribute @Valid RecommendedSpotListRequest request
	) {
		var point = PointConverter.convert(request.lat(), request.lon());
		var response = spotService.getRecommendedSpotList(new RecommendedSpotListServiceRequest(point, request.radius()));

		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
