package com.tf4.photospot.spot.domain;

import java.util.Optional;

import org.locationtech.jts.geom.Point;

public interface MapApiClient {
	Optional<String> findAddressByCoordinate(Point coord);

	Optional<Point> findCoordinateByAddress(String address);
}
