package com.tf4.photospot.photo.domain;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Directory {

	POST_FOLDER("post", "temp/"),
	PROFILE_FOLDER("profile", "member_profile_images/");

	private final String type;
	private final String folder;

	public static Optional<Directory> findByType(String requestType) {
		return Arrays.stream(Directory.values())
			.filter(folder -> folder.type.equals(requestType))
			.findFirst();
	}
}
