package com.tf4.photospot.map.application.response.kakao;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tf4.photospot.global.util.PointConverter;

@JsonNaming(value = SnakeCaseStrategy.class)
public record KakaoSearchAddressResponse(
	Meta meta,
	List<Document> documents
) {
	public Optional<Point> findCoordinate() {
		if (!existResult()) {
			return Optional.empty();
		}
		return Optional.ofNullable(documents.get(0))
			.map(Document::roadAddress)
			.map(this::toCoordinate);
	}

	private boolean existResult() {
		return meta != null && meta.totalCount() > 0 && !documents.isEmpty();
	}

	private Point toCoordinate(Document.RoadAddress roadAddress) {
		if (roadAddress == null || roadAddress.x == null || roadAddress.y == null) {
			return null;
		}
		return PointConverter.convert(Double.valueOf(roadAddress.x), Double.valueOf(roadAddress.y));
	}

	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record Meta(
		Integer totalCount,
		Integer pageableCount,
		Boolean isEnd
	) {
	}

	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record Document(
		String addressName,
		String addressType, //REGION(지명), ROAD(도로명), REGION_ADDR(지번 주소), ROAD_ADDR(도로명 주소)
		String x,
		String y,
		RoadAddress roadAddress,
		Address address
	) {
		@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
		public record Address(
			String addressName,
			String region1DepthName,
			String region2DepthName,
			String region3DepthName,
			String x,
			String y
		) {
		}

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
		}
	}
}
