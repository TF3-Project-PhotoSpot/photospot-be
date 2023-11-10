package com.tf4.photospot.spot.application.request;

import org.locationtech.jts.geom.Point;

public record RecommendedSpotListServiceRequest(
	Point point,
	Long radius
) {
}
