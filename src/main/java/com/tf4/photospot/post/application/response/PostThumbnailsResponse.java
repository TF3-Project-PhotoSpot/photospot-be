package com.tf4.photospot.post.application.response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record PostThumbnailsResponse(
	Map<Long, List<PostThumbnailResponse>> postThumbnails
) {
	public List<PostThumbnailResponse> getPostThumbnails(Long spotId) {
		return postThumbnails().getOrDefault(spotId, Collections.emptyList());
	}
}
