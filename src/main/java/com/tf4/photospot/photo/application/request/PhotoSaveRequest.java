package com.tf4.photospot.photo.application.request;

import java.time.LocalDate;

import org.locationtech.jts.geom.Point;

public record PhotoSaveRequest(
	String photoUrl,
	Point point,
	LocalDate takenAt
) {
}
