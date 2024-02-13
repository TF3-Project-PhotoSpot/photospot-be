package com.tf4.photospot.post.application.request;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;

import lombok.Builder;

public record PostSearchCondition(
	Long spotId,
	Long userId,
	PostSearchType type,
	Pageable pageable
) {
	private static final Set<String> SORTABLE_PROPERTIES = Set.of("id", "likeCount");

	@Builder
	public PostSearchCondition {
		if (containsInvalidOrder(pageable.getSort())) {
			throw new ApiException(CommonErrorCode.CANNOT_SORTED_PROPERTY);
		}
	}

	private boolean containsInvalidOrder(Sort sort) {
		return sort.stream().anyMatch(order -> !SORTABLE_PROPERTIES.contains(order.getProperty()));
	}
}
