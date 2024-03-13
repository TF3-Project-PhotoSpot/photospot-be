package com.tf4.photospot.photo.domain;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Extension {

	JPEG("jpeg"),
	WEBP("webp");

	private final String type;

	public static Optional<Extension> getPhotoExtension(String contentType) {
		return Arrays.stream(Extension.values())
			.filter(extension -> extension.type.equals(contentType))
			.findFirst();
	}
}
