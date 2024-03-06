package com.tf4.photospot.bookmark.presentation.request;

import java.util.List;

import jakarta.validation.constraints.Positive;

public record RemoveBookmarkHttpRequest(
	List<@Positive Long> bookmarkIds
) {
}
