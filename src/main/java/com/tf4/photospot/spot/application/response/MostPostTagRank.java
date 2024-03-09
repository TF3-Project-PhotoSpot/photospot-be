package com.tf4.photospot.spot.application.response;

import lombok.Builder;

public record MostPostTagRank(
	Long id,
	int count,
	String name,
	String iconUrl
) {
	@Builder
	public MostPostTagRank {
	}
}
