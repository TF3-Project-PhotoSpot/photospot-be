package com.tf4.photospot.auth.presentation.request;

import jakarta.annotation.Nullable;

public record UnlinkRequest(
	@Nullable
	String authorizationCode
) {
}
