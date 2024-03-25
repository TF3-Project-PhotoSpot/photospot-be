package com.tf4.photospot.photo.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.photo.domain.Extension;

public class FileUtils {
	private static final String NAME_SEPARATOR = "_";
	private static final String EXTENSION_SEPARATOR = ".";

	public static String generateNewFileName(String originalFileName) {
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uuid = UUID.randomUUID().toString().substring(0, 8);
		return now + NAME_SEPARATOR + uuid + EXTENSION_SEPARATOR + extractExtension(originalFileName).getType();
	}

	public static Extension extractExtension(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		return Extension.getPhotoExtension(extension)
			.orElseThrow(() -> new ApiException(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION));
	}
}
