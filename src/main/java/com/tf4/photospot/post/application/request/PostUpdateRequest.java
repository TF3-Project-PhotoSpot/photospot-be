package com.tf4.photospot.post.application.request;

import java.util.List;

import com.tf4.photospot.post.presentation.request.PostUpdateHttpRequest;

public record PostUpdateRequest(
	Long userId,
	Long postId,
	List<Long> tags,
	List<Long> mentions,
	boolean isPrivate
) {

	public PostUpdateRequest {
		tags = distinctList(tags);
		mentions = distinctList(mentions);
	}

	public static PostUpdateRequest of(Long userId, Long postId, PostUpdateHttpRequest request) {
		return new PostUpdateRequest(
			userId, postId,
			request.tags(),
			request.mentions(),
			request.isPrivate()
		);
	}

	private static List<Long> distinctList(List<Long> list) {
		return list.stream().distinct().toList();
	}
}
