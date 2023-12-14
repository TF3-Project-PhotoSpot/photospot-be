package com.tf4.photospot.spot.application.request;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;

public record RecommendedSpotsRequest(
	Point coord,
	Integer radius,
	Integer postPreviewCount,
	Pageable pageable
) {
}
