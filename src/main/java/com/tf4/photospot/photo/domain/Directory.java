package com.tf4.photospot.photo.domain;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Directory {

	TEMP_FOLDER("temp", "temp/"),
	POST_FOLDER("post", "post_images/"),
	PROFILE_FOLDER("profile", "profile_images/");

	private final String folder;
	private final String path;

	public static Optional<Directory> findByFolder(String folder) {
		return Arrays.stream(Directory.values())
			.filter(dir -> dir.folder.equals(folder))
			.findFirst();
	}
}
