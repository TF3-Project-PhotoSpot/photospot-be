package com.tf4.photospot.auth.presentation.request;

import jakarta.annotation.Nullable;

public record UnlinkRequest(
	String provider,

	@Nullable
	Boolean isLinked,

	@Nullable
	String authorizationCode
) {
}
