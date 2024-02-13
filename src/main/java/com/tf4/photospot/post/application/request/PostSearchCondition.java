package com.tf4.photospot.post.application.request;

import org.springframework.data.domain.Pageable;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;

import lombok.Builder;

public record PostSearchCondition(
	Long spotId,
	Long userId,
	PostSearchType type,
	Pageable pageable
) {
	@Builder
	public PostSearchCondition {
		if (type == null) {
			throw new ApiException(PostErrorCode.REQUIRE_POST_SEARCH_TYPE);
		}
		type.verifyOrders(pageable.getSort());
	}
}
