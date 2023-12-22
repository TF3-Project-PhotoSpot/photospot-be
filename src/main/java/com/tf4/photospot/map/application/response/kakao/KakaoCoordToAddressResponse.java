package com.tf4.photospot.map.application.response.kakao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoCoordToAddressResponse(
	Meta meta,
	List<Document> documents
) {
	public KakaoCoordToAddressResponse {
		if (meta == null) {
			meta = new Meta(0);
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

	@JsonNaming(SnakeCaseStrategy.class)
	public record Meta(
		Integer totalCount
	) {
	}

	@JsonNaming(SnakeCaseStrategy.class)
	public record Document(
		RoadAddress roadAddress,
		Address address
	) {
		public static final Document DEFAULT_DOCUMENT = new Document(null, null);

		public Document {
			if (roadAddress == null) {
				roadAddress = RoadAddress.DEFAULT_ROAD_ADDRESS;
			}
			if (address == null) {
				address = Address.DEFAULT_ADDRESS;
			}
		}

		@Builder
		@JsonNaming(SnakeCaseStrategy.class)
		public record RoadAddress(
			String addressName,
			String region1DepthName,
			String region2DepthName,
			String region3DepthName,
			String roadName,
			String buildingName,
			String zoneNo
		) {
			public static final RoadAddress DEFAULT_ROAD_ADDRESS = RoadAddress.builder().build();
		}

		@Builder
		@JsonNaming(SnakeCaseStrategy.class)
		public record Address(
			String addressName,
			String region1DepthName,
			String region2DepthName,
			String region3DepthName
		) {
			public static final Address DEFAULT_ADDRESS = Address.builder().build();
		}
	}
}
