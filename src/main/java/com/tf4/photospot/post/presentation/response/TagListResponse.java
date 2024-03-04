package com.tf4.photospot.post.presentation.response;

import java.util.List;

import com.tf4.photospot.post.application.response.TagResponse;

public record TagListResponse(
	List<TagResponse> tags
) {
}
