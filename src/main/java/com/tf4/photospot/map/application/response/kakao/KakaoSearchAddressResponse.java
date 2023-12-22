package com.tf4.photospot.map.application.response.kakao;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@Builder
@JsonNaming(value = SnakeCaseStrategy.class)
public record KakaoSearchAddressResponse(
	Meta meta,
	List<Document> documents
) {
	public KakaoSearchAddressResponse {
		if (meta == null) {
			meta = new Meta(0, 0, true);
		}
		if (documents == null) {
			documents = Collections.emptyList();
		}
	}

	public Optional<Document> findFirstDocument() {
		if (documents.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(documents.get(0));
	}

	public boolean existResult() {
		return meta != null && meta.totalCount() > 0 && !documents.isEmpty();
	}

	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record Meta(
		Integer totalCount,
		Integer pageableCount,
		Boolean isEnd
	) {
	}

	@Builder
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record Document(
		String addressName,
		String addressType, //REGION(지명), ROAD(도로명), REGION_ADDR(지번 주소), ROAD_ADDR(도로명 주소)
		String x,
		String y,
		RoadAddress roadAddress,
		Address address
	) {
		public static final Document DEFAULT_DOCUMENT = Document.builder().build();

		public Document {
			if (roadAddress == null) {
				roadAddress = RoadAddress.DEFAULT_ROAD_ADDRESS;
			}
			if (address == null) {
				address = Address.DEFAULT_ADDRESS;
			}
		}

		@Builder
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public record Address(
			String addressName,
			String region1DepthName,
			String region2DepthName,
			String region3DepthName,
			String x,
			String y
		) {
			public static final Address DEFAULT_ADDRESS = Address.builder().build();
		}

		@Builder
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public record RoadAddress(
			String addressName,
			String region1DepthName,
			String region2DepthName,
			String region3DepthName,
			String roadName,
			String buildingName,
			String zoneNo,
			String x,
			String y
		) {
			public static final RoadAddress DEFAULT_ROAD_ADDRESS = RoadAddress.builder().build();
		}
	}
}
