package com.tf4.photospot.map.application.response.kakao;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoCoordToAddressResponse(
	Meta meta,
	List<Document> documents
) {
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
		}

		@JsonNaming(SnakeCaseStrategy.class)
		public record Address(
			String addressName,
			String region1DepthName,
			String region2DepthName,
			String region3DepthName
		) {
		}
	}

	public Optional<String> findAddressName() {
		if (!existResult()) {
			return Optional.empty();
		}
		return Optional.ofNullable(documents.get(0))
			.map(Document::roadAddress)
			.map(Document.RoadAddress::addressName);
	}

	private boolean existResult() {
		return meta != null && meta.totalCount() == 1 && !documents.isEmpty();
	}
}
