package com.tf4.photospot.auth.presentation.response;

public record LoginTokenResponse(boolean hasLoggedInBefore, String accessToken, String refreshToken) {
}
