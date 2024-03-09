package com.tf4.photospot.bookmark.presentation.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record AddBookmarkHttpRequest(
	@Positive
	Long spotId,
	@Size(min = 1, max = 10, message = "북마크 이름은 1글자 이상 10글자 이하입니다.")
	String name,
	@Size(max = 30, message = "북마크 설명은 30자 이하로 입력해주세요.")
	String description
) {
	@Builder
	public AddBookmarkHttpRequest {
	}
}
