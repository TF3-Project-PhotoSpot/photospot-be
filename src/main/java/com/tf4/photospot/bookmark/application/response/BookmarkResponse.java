package com.tf4.photospot.bookmark.application.response;

import java.util.Collections;
import java.util.List;

import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.post.application.response.PostPreviewResponse;

import lombok.Builder;
import software.amazon.awssdk.utils.CollectionUtils;

public record BookmarkResponse(
	Long id,
	Long spotId,
	String name,
	String description,
	List<String> photoUrls
) {
	@Builder
	public BookmarkResponse {
	}

	public static BookmarkResponse of(Bookmark bookmark, List<PostPreviewResponse> postPreviewResponses) {
		return BookmarkResponse.builder()
			.id(bookmark.getId())
			.spotId(bookmark.getSpotId())
			.name(bookmark.getName())
			.description(bookmark.getDescription())
			.photoUrls(getPhotoUrls(postPreviewResponses))
			.build();
	}

	private static List<String> getPhotoUrls(List<PostPreviewResponse> postPreviews) {
		if (CollectionUtils.isNullOrEmpty(postPreviews)) {
			return Collections.emptyList();
		}
		return postPreviews.stream().map(PostPreviewResponse::photoUrl).toList();
	}
}
