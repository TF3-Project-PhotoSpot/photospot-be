package com.tf4.photospot.post.presentation.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BubbleInfoDto(
	@Size(max = 60, message = "최대 60자까지 입력 가능합니다.")
	String text,

	@Positive(message = "버블 위치 좌표는 0보다 커야 합니다.")
	double x,

	@Positive(message = "버블 위치 좌표는 0보다 커야 합니다.")
	double y
) {
}
