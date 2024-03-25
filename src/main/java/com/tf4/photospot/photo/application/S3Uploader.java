package com.tf4.photospot.photo.application;

import java.io.IOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.photo.util.FileUtils;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Component
@RequiredArgsConstructor
public class S3Uploader {
	private final S3Template s3Template;

	private final S3Client s3Client;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	public String upload(MultipartFile file, String folder) {
		validFile(file);
		String fileName = validAndGetFileName(file.getOriginalFilename());
		String fileKey = getFileKey(fileName, folder);
		try {
			ObjectMetadata objectMetadata = generateObjectMetadata(
				FileUtils.extractExtension(Objects.requireNonNull(fileName)).getType(), file.getSize());
			return s3Template.upload(bucket, fileKey, file.getInputStream(), objectMetadata).getURL().toString();
		} catch (IOException ex) {
			throw new ApiException(S3UploaderErrorCode.UNEXPECTED_GET_URL_FAIL);
		}
	}

	private void validFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(S3UploaderErrorCode.EMPTY_FILE);
		}
	}

	private String getFileKey(String originalFileName, String folder) {
		S3Directory s3Directory = S3Directory.findByFolder(folder)
			.orElseThrow(() -> new ApiException(S3UploaderErrorCode.NOT_FOUND_FOLDER));
		return s3Directory.getPath() + FileUtils.generateNewFileName(validAndGetFileName(originalFileName));
	}

	private String validAndGetFileName(String originalFileName) {
		if (!StringUtils.hasText(originalFileName)) {
			throw new ApiException(S3UploaderErrorCode.INVALID_FILE_NAME);
		}
		return originalFileName;
	}

	private ObjectMetadata generateObjectMetadata(String extension, Long length) {
		return new ObjectMetadata.Builder()
			.contentType(extension)
			.contentLength(length)
			.build();
	}

	public String copyToOtherDirectory(String photoUrl, S3Directory fromDirectory, S3Directory toDirectory) {
		String fileName = extractFileName(photoUrl);
		String sourceKey = fromDirectory.getPath() + fileName;
		String destinationKey = toDirectory.getPath() + fileName;
		return copyToOtherDirectory(sourceKey, destinationKey);
	}

	private String copyToOtherDirectory(String sourceKey, String destinationKey) {
		executeS3Copy(sourceKey, destinationKey);
		try {
			return s3Template.download(bucket, destinationKey).getURL().toString();
		} catch (Exception ex) {
			throw new ApiException(S3UploaderErrorCode.UNEXPECTED_GET_URL_FAIL);
		}
	}

	private String extractFileName(String photoUrl) {
		int lastSeparateIndex = photoUrl.lastIndexOf("/");
		return photoUrl.substring(lastSeparateIndex + 1);
	}

	private void executeS3Copy(String sourceKey, String destinationKey) {
		try {
			s3Client.copyObject(CopyObjectRequest.builder()
				.sourceBucket(bucket)
				.sourceKey(sourceKey)
				.destinationBucket(bucket)
				.destinationKey(destinationKey)
				.build());
		} catch (NoSuchKeyException ex) {
			throw new ApiException(S3UploaderErrorCode.NOT_FOUND_FILE);
		} catch (Exception ex) {
			throw new ApiException(S3UploaderErrorCode.UNEXPECTED_COPY_FAIL);
		}
	}

	public void deleteFile(String photoUrl, S3Directory directory) {
		delete(directory.getPath() + extractFileName(photoUrl));
	}

	private void delete(String sourceKey) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(sourceKey).build());
		} catch (NoSuchKeyException ex) {
			throw new ApiException(S3UploaderErrorCode.NOT_FOUND_FILE);
		} catch (Exception ex) {
			throw new ApiException(S3UploaderErrorCode.UNEXPECTED_DELETE_FAIL);
		}
	}
}
