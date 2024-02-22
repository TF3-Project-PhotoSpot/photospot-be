package com.tf4.photospot.album.presentation.request;

import java.util.List;

import jakarta.validation.constraints.Positive;

public record PostIdListHttpRequest(
	List<@Positive Long> postIds
) {
}
