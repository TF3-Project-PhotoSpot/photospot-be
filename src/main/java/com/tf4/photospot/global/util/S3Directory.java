package com.tf4.photospot.global.util;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3Directory {

	TEMP_FOLDER("temp", "temp/"),
	POST_FOLDER("post", "post_images/"),
	PROFILE_FOLDER("profile", "user_profile/");

	private final String folder;
	private final String path;

	public static Optional<S3Directory> findByFolder(String folder) {
		return Arrays.stream(S3Directory.values())
			.filter(dir -> dir.folder.equals(folder))
			.findFirst();
	}
}
