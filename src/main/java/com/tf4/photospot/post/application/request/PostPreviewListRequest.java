package com.tf4.photospot.post.application.request;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;

public record PostPreviewListRequest(
	Long spotId,
	Pageable pageable
) {
	private static final Set<String> sortableProperties = Set.of("id", "likeCount");

	public PostPreviewListRequest {
		if (containsInvalidOrder(pageable.getSort())) {
			throw new ApiException(CommonErrorCode.CANNOT_SORTED_PROPERTY);
		}
	}

	private boolean containsInvalidOrder(Sort sort) {
		return sort.stream().anyMatch(order -> !sortableProperties.contains(order.getProperty()));
	}

	public static PostPreviewListRequest createLatestPostsRequest(Long spotId, int previewCount) {
		return new PostPreviewListRequest(spotId,
			PageRequest.of(0, previewCount, Sort.by(Sort.Direction.DESC, "id")));
	}
}
