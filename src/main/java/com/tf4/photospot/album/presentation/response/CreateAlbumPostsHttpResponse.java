package com.tf4.photospot.album.presentation.response;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tf4.photospot.album.application.response.CreateAlbumPostResponse;
import com.tf4.photospot.album.presentation.request.PostIdListHttpRequest;

public record CreateAlbumPostsHttpResponse(
	List<Long> failedPostIds
) {
	public static CreateAlbumPostsHttpResponse of(PostIdListHttpRequest request,
		List<CreateAlbumPostResponse> createAlbumPosts) {
		final Set<Long> addedPostIds = createAlbumPosts.stream()
			.map(CreateAlbumPostResponse::postId)
			.collect(Collectors.toSet());
		final List<Long> failedPostIds = request.postIds().stream()
			.filter(postId -> !addedPostIds.contains(postId))
			.toList();
		return new CreateAlbumPostsHttpResponse(failedPostIds);
	}
}
