package com.tf4.photospot.global.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tf4.photospot.photo.domain.Directory;
import com.tf4.photospot.photo.domain.Extension;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class S3Uploader {

	private static final String NAME_SEPARATOR = "_";
	private static final String EXTENSION_SEPARATOR = ".";

	private final AmazonS3 amazonS3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public String upload(MultipartFile file, String type) {
		validateFileExists(file);
		Directory directory = Directory.findByType(type).orElseThrow(() -> new RuntimeException());
		String fileKey = directory.getFolder() + generateNewFileName(file.getContentType());
		try {
			ObjectMetadata objectMetadata = generateObjectMetadata(file);
			amazonS3Client.putObject(
				new PutObjectRequest(bucket, fileKey, file.getInputStream(), objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (Exception ex) {
			throw new RuntimeException();
		}
		return amazonS3Client.getUrl(bucket, fileKey).toString();
	}

	private void validateFileExists(MultipartFile file) {
		if (file.isEmpty()) {
			throw new RuntimeException();
		}
	}

	private String generateNewFileName(String contentType) {
		Extension extension = Extension.getPhotoExtension(contentType)
			.orElseThrow(() -> new RuntimeException());
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uuid = UUID.randomUUID().toString().substring(0, 8);

		return now + NAME_SEPARATOR + uuid + EXTENSION_SEPARATOR + extension.getType();
	}

	private ObjectMetadata generateObjectMetadata(MultipartFile file) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(file.getContentType());
		objectMetadata.setContentLength(file.getSize());
		return objectMetadata;
	}
}
