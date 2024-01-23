package com.tf4.photospot.post.application.request;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;

public record PostListRequest(
	Long spotId,
	Pageable pageable
) {
	private static final Set<String> sortableProperties = Set.of("id", "likeCount");

	public PostListRequest {
		if (containsInvalidOrder(pageable.getSort())) {
			throw new ApiException(CommonErrorCode.CANNOT_SORTED_PROPERTY);
		}
	}

	private boolean containsInvalidOrder(Sort sort) {
		return sort.stream().anyMatch(order -> !sortableProperties.contains(order.getProperty()));
	}
}
