package com.tf4.photospot.post.application.request;

import java.util.Set;
import java.util.function.Predicate;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PostSearchType {
	MY_POSTS(Set.of("id"), cond -> cond.userId() != null),
	POSTS_OF_SPOT(Set.of("id", "likeCount"), cond -> cond.spotId() != null);

	private final Set<String> sortableProperties;
	private final Predicate<PostSearchCondition> verifyRequiredCondition;

	public void verify(PostSearchCondition searchCondition) {
		final boolean sortable = searchCondition.pageable().getSort().stream()
			.allMatch(order -> sortableProperties.contains(order.getProperty()));
		final boolean hasRequiredCondition = verifyRequiredCondition.test(searchCondition);
		if (sortable && hasRequiredCondition) {
			return;
		}
		throw new ApiException(CommonErrorCode.INVALID_SEARCH_CONDITION);
	}
}
