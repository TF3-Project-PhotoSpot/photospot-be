package com.tf4.photospot.post.application.request;

import java.util.List;

import org.springframework.util.StringUtils;

import com.tf4.photospot.post.presentation.request.PostUpdateHttpRequest;

public record PostUpdateRequest(Long userId, Long postId, List<Long> tags, List<Long> mentions, String detailAddress) {

	public PostUpdateRequest {
		detailAddress = convertBlankToNull(detailAddress);
	}

	public static PostUpdateRequest of(Long userId, Long postId, PostUpdateHttpRequest request) {
		return new PostUpdateRequest(userId, postId, request.tags(), request.mentions(), request.detailAddress());
	}

	private static String convertBlankToNull(String str) {
		if (StringUtils.hasText(str)) {
			return str;
		}
		return null;
	}
}
