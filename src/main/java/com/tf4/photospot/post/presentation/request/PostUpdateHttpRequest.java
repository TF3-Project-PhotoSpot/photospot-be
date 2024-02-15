package com.tf4.photospot.post.presentation.request;

import java.util.List;

public record PostUpdateHttpRequest(
	List<Long> tags,

	List<Long> mentions,

	boolean isPrivate
) {
}
