package com.tf4.photospot.spot.application.request;

import org.locationtech.jts.geom.Point;

public record FindSpotRequest(
	Point coord
) {
}
