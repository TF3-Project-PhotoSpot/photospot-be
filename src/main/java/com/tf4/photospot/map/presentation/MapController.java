package com.tf4.photospot.map.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.util.PointConverter;
import com.tf4.photospot.map.application.MapService;
import com.tf4.photospot.map.application.response.SearchByAddressResponse;
import com.tf4.photospot.map.application.response.SearchByCoordResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/map")
@RestController
@RequiredArgsConstructor
public class MapController {
	private final MapService mapService;

	@GetMapping("/search/location")
	public ApiResponse<SearchByAddressResponse> searchLocation(
		@ModelAttribute @Valid CoordinateDto coord
	) {
		SearchByCoordResponse searchByCoordResponse = mapService.searchByCoord(PointConverter.convert(coord));
		return ApiResponse.success(mapService.searchByAddress(
			searchByCoordResponse.address(),
			searchByCoordResponse.roadAddress()));
	}

}
