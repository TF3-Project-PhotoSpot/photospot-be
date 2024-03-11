package com.tf4.photospot.photo.application;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.photo.domain.Extension;
import com.tf4.photospot.photo.domain.S3Directory;

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

	private static final String NAME_SEPARATOR = "_";
	private static final String EXTENSION_SEPARATOR = ".";

	private final S3Template s3Template;

	private final S3Client s3Client;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	public String upload(MultipartFile file, String folder) {
		S3Directory s3Directory = S3Directory.findByFolder(folder)
			.orElseThrow(() -> new ApiException(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION));
		String fileKey = s3Directory.getPath() + generateNewFileName(validFileAndGetFileName(file));
		try {
			ObjectMetadata objectMetadata = generateObjectMetadata(file);
			return s3Template.upload(bucket, fileKey, file.getInputStream(), objectMetadata).getURL().toString();
		} catch (IOException ex) {
			throw new ApiException(S3UploaderErrorCode.UNEXPECTED_GET_URL_FAIL);
		} catch (Exception ex) {
			throw new ApiException(S3UploaderErrorCode.UNEXPECTED_UPLOAD_FAIL);
		}
	}

	private String validFileAndGetFileName(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(S3UploaderErrorCode.EMPTY_FILE);
		}
		return file.getOriginalFilename();
	}

	private String generateNewFileName(String originalFileName) {
		String contentType = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
		Extension extension = Extension.getPhotoExtension(contentType)
			.orElseThrow(() -> new ApiException(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION));
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String uuid = UUID.randomUUID().toString().substring(0, 8);

		return now + NAME_SEPARATOR + uuid + EXTENSION_SEPARATOR + extension.getType();
	}

	private ObjectMetadata generateObjectMetadata(MultipartFile file) {
		return new ObjectMetadata.Builder().contentType(file.getContentType()).contentLength(file.getSize()).build();
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
