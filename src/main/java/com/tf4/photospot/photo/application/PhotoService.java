package com.tf4.photospot.photo.application;

import java.time.LocalDate;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.photo.application.response.PhotoSaveResponse;
import com.tf4.photospot.photo.application.response.PhotoUploadResponse;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.photo.domain.S3Directory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

	private final PhotoRepository photoRepository;
	private final S3Uploader s3Uploader;

	public PhotoUploadResponse upload(MultipartFile file) {
		return new PhotoUploadResponse(s3Uploader.upload(file, S3Directory.TEMP_FOLDER.getFolder()));
	}

	@Transactional
	public PhotoSaveResponse save(String photoUrl, Point point, LocalDate takenAt) {
		String renewalUrl = moveFromTempToPostFolder(photoUrl);
		Photo photo = Photo.builder()
			.photoUrl(renewalUrl)
			.coord(point)
			.takenAt(takenAt)
			.build();
		Long postPhotoId = photoRepository.save(photo).getId();
		return new PhotoSaveResponse(postPhotoId);
	}

	private String moveFromTempToPostFolder(String originKey) {
		String fileName = extractFileName(originKey);
		String sourceKey = S3Directory.TEMP_FOLDER.getPath() + fileName;
		String destinationKey = S3Directory.POST_FOLDER.getPath() + fileName;
		return s3Uploader.moveFolder(sourceKey, destinationKey);
	}

	private String extractFileName(String photoUrl) {
		int lastSeparateIndex = photoUrl.lastIndexOf("/");
		return photoUrl.substring(lastSeparateIndex + 1);
	}

}
