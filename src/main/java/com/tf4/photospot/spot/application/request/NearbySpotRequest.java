package com.tf4.photospot.spot.application.request;

import org.locationtech.jts.geom.Point;

public record NearbySpotRequest(
	Point coord,
	Integer radius
) {
}
