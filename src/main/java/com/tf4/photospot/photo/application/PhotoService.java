package com.tf4.photospot.photo.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tf4.photospot.photo.domain.Directory;
import com.tf4.photospot.photo.domain.Extension;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.photo.presentation.response.PhotoUploadResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoService {

	private static final String NAME_SEPARATOR = "_";
	private static final String EXTENSION_SEPARATOR = ".";

	private final PhotoRepository photoRepository;
	private final AmazonS3 amazonS3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	// 방명록 사진 업로드
	@Transactional
	public PhotoUploadResponse savePostPhoto(MultipartFile file, Point point, LocalDate takenAt) {
		String photoUrl = upload(file, Directory.POST_FOLDER.getFolder());
		Photo photo = Photo.builder()
			.photoUrl(photoUrl)
			.coord(point)
			.takenAt(takenAt)
			.build();
		Long postPhotoId = photoRepository.save(photo).getId();
		return new PhotoUploadResponse(postPhotoId);
	}

	// 그 외 사진 업로드(폴더로 구분)
	public String uploadOtherPhoto(MultipartFile file, String type) {
		Directory directory = Directory.findByType(type).orElseThrow(() -> new RuntimeException());
		return upload(file, directory.getFolder());
	}

	// Todo : exception
	private String upload(MultipartFile file, String folder) {
		validateFileExists(file);
		String fileKey = folder + generateNewFileName(file.getContentType());
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
