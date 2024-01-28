package com.tf4.photospot.post.application.response;

import com.tf4.photospot.user.domain.User;

import lombok.Builder;

public record WriterResponse(
	Long id,
	String nickname,
	String profileUrl
) {
	@Builder
	public WriterResponse {
	}

	public static WriterResponse from(User user) {
		return WriterResponse.builder()
			.id(user.getId())
			.nickname(user.getNickname())
			.profileUrl(user.getProfileUrl())
			.build();
	}
}
