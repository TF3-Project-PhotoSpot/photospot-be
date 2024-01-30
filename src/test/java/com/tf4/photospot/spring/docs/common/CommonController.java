package com.tf4.photospot.spring.docs.common;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tf4.photospot.global.argument.CoordinateValidator;
import com.tf4.photospot.global.dto.ApiResponse;
import com.tf4.photospot.global.dto.ValidationError;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;
import com.tf4.photospot.global.exception.domain.MapErrorCode;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.global.exception.domain.SpotErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;

@RequestMapping("/common")
@RestController
public class CommonController {
	@GetMapping("/success")
	public ApiResponse success() {
		return ApiResponse.SUCCESS;
	}

	@GetMapping("/error")
	public ResponseEntity<ApiResponse> error() {
		return ResponseEntity.badRequest()
			.body(ApiResponse.builder()
				.code(CommonErrorCode.INVALID_PARAMETER.name())
				.message(CommonErrorCode.INVALID_PARAMETER.getMessage())
				.errors(List.of(
					new ValidationError("lat", "180", CoordinateValidator.COORD_INVALID_RANGE),
					new ValidationError("lon", "90", CoordinateValidator.COORD_INVALID_RANGE)))
				.build()
			);
	}

	@GetMapping("/errorcodes")
	public Map<String, ErrorCodeResponse> errorCode() {
		return getErrorCodeResponseMap();
	}

	protected static Map<String, ErrorCodeResponse> getErrorCodeResponseMap() {
		var errorCodeGroups = Stream.of(
			CommonErrorCode.values(),
			MapErrorCode.values(),
			AuthErrorCode.values(),
			S3UploaderErrorCode.values(),
			SpotErrorCode.values(),
			UserErrorCode.values()
		);
		return errorCodeGroups.flatMap(Arrays::stream)
			.collect(Collectors.toMap(
				Enum::name,
				errorCode -> new ErrorCodeResponse(
					errorCode.getClass().getSimpleName(),
					errorCode.name(), //TODO: name -> code
					errorCode.getStatusCode().value(),
					errorCode.getMessage()),
				(exist, replace) -> exist, LinkedHashMap::new));
	}
}
