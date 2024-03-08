package com.tf4.photospot.bookmark.application.response;

import org.locationtech.jts.geom.Point;

import com.querydsl.core.annotations.QueryProjection;

public record BookmarkCoord(
	Long bookmarkFolderId,
	String bookmarkFolderColor,
	Long bookmarkId,
	Long spotId,
	Point coord
) {
	@QueryProjection
	public BookmarkCoord {
	}
}
