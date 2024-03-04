package com.tf4.photospot.bookmark.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record CreateBookmarkFolderHttpRequest(
	@Size(min = 1, max = 10, message = "폴더 이름은 1글자 이상 10글자 이하입니다.")
	String name,
	@Size(max = 30, message = "폴더 설명은 30자 이하로 입력해주세요.")
	String description,
	@NotBlank(message = "색상은 필수입니다.")
	String color
) {
	@Builder
	public CreateBookmarkFolderHttpRequest {
	}
}
