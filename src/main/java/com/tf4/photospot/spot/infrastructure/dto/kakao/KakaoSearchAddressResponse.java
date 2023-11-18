package com.tf4.photospot.spot.infrastructure.dto.kakao;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.tf4.photospot.global.util.PointConverter;

@JsonNaming(value = SnakeCaseStrategy.class)
public record KakaoSearchAddressResponse(
	Meta meta,
	List<Document> documents
) {
	public Optional<Point> findCoordinate() {
		if (meta.totalCount() == 0 || documents.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(documents.get(0))
			.map(this::toCoordinate);
	}

	private Point toCoordinate(Document document) {
		return PointConverter.convert(Double.valueOf(document.y()), Double.valueOf(document.x()));
	}
}
