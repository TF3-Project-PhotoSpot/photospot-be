package com.tf4.photospot.post.application.response;

import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.Tag;

import lombok.Builder;

public record TagResponse(
	Long tagId,
	String iconUrl,
	String tagName
) {
	@Builder
	public TagResponse {
	}

	public static TagResponse from(PostTag postTag) {
		final Tag tag = postTag.getTag();
		return TagResponse.builder()
			.tagId(tag.getId())
			.iconUrl(tag.getIconUrl())
			.tagName(tag.getName())
			.build();
	}
}
