package com.tf4.photospot.user.application.response;

import com.tf4.photospot.user.domain.User;

import lombok.Builder;

@Builder
public record UserInfoResponse(
	Long userId,
	String nickname,
	String profileUrl,
	String provider
) {
	public static UserInfoResponse of(User user) {
		return UserInfoResponse.builder()
			.userId(user.getId())
			.nickname(user.getNickname())
			.profileUrl(user.getProfileUrl())
			.provider(user.getProviderType())
			.build();
	}
}
