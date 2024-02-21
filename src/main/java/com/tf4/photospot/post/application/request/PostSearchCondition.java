package com.tf4.photospot.post.application.request;

import org.springframework.data.domain.Pageable;

import lombok.Builder;

public record PostSearchCondition(
	Long spotId,
	Long userId,
	PostSearchType type,
	Pageable pageable
) {

	@Builder
	public PostSearchCondition(Long spotId, Long userId, PostSearchType type, Pageable pageable) {
		this.spotId = spotId;
		this.userId = userId;
		this.type = type;
		this.pageable = pageable;
		type.verify(this);
	}
}
