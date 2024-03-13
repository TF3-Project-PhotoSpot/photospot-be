package com.tf4.photospot.post.application.response;

import com.tf4.photospot.photo.domain.Bubble;

import lombok.Builder;

public record BubbleResponse(
	String text,
	double x,
	double y
) {

	@Builder
	public BubbleResponse {
	}

	public static BubbleResponse from(Bubble bubble) {
		if (bubble == null) {
			return null;
		}
		return BubbleResponse.builder()
			.text(bubble.getText())
			.x(bubble.getPosX())
			.y(bubble.getPosY())
			.build();
	}
}
