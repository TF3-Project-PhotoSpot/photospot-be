package com.tf4.photospot.post.application.request;

import java.util.Set;

import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PostSearchType {
	MY_POSTS(Set.of("id")),
	POSTS_OF_SPOT(Set.of("id", "likeCount"));

	private final Set<String> sortableProperties;

	public void verifyOrders(Sort sort) {
		final boolean cannotSort = sort.stream().anyMatch(order -> !sortableProperties.contains(order.getProperty()));
		if (cannotSort) {
			throw new ApiException(CommonErrorCode.CANNOT_SORTED_PROPERTY);
		}
	}
}
