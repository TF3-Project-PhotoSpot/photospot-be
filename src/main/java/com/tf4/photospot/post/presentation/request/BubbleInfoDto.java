package com.tf4.photospot.post.presentation.request;

import jakarta.validation.constraints.Positive;

public record BubbleInfoDto(
	String text,

	@Positive(message = "버블 위치 좌표는 0보다 커야 합니다.")
	long x,

	@Positive(message = "버블 위치 좌표는 0보다 커야 합니다.")
	long y
) {
}
