package com.tf4.photospot.post.application.response;

import com.tf4.photospot.user.domain.User;

import lombok.Builder;

public record WriterResponse(
	Long id,
	Boolean isWriter,
	String nickname,
	String profileUrl
) {
	@Builder
	public WriterResponse {
	}

	public static WriterResponse from(User writer, Long readUserId) {
		return WriterResponse.builder()
			.id(writer.getId())
			.isWriter(writer.getId().equals(readUserId))
			.nickname(writer.getNickname())
			.profileUrl(writer.getProfileUrl())
			.build();
	}
}
