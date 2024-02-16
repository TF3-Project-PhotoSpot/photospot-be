package com.tf4.photospot.post.application.response;

import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.user.domain.User;

import lombok.Builder;

public record MentionResponse(
	Long userId,
	String nickname
) {
	@Builder
	public MentionResponse {
	}

	public static MentionResponse from(Mention mention) {
		final User user = mention.getMentionedUser();
		return MentionResponse.builder()
			.userId(user.getId())
			.nickname(user.getNickname())
			.build();
	}
}
