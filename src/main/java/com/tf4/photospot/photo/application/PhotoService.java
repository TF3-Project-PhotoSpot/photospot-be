package com.tf4.photospot.photo.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.util.S3Directory;
import com.tf4.photospot.global.util.S3Uploader;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.photo.presentation.response.PhotoSaveResponse;
import com.tf4.photospot.post.domain.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoService {

	private final PhotoRepository photoRepository;
	private final PostRepository postRepository;
	private final S3Uploader s3Uploader;

	@Transactional
	public PhotoSaveResponse save(MultipartFile file, Point point, LocalDate takenAt) {
		String photoUrl = s3Uploader.upload(file, S3Directory.TEMP_FOLDER.getFolder());
		Photo photo = Photo.builder()
			.photoUrl(photoUrl)
			.coord(point)
			.takenAt(takenAt)
			.build();
		Long postPhotoId = photoRepository.save(photo).getId();
		return new PhotoSaveResponse(postPhotoId);
	}

	// 매일 새벽 4시 스케줄링
	@Scheduled(cron = "0 0 04 * * ?")
	@Transactional
	public void scheduleToMovePhotos() {
		LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
		List<Photo> postPhotos = photoRepository.findAllByCreatedAtGreaterThanEqual(twentyFourHoursAgo);
		for (Photo postPhoto : postPhotos) {
			moveFromTempToPost(postPhoto);
		}
	}

	private void moveFromTempToPost(Photo photo) {
		if (postRepository.existsByPhotoId(photo.getId())) {
			String fileName = extractFileName(photo.getPhotoUrl());
			String sourceKey = S3Directory.TEMP_FOLDER.getPath() + fileName;
			String destinationKey = S3Directory.POST_FOLDER.getPath() + fileName;
			String renewalUrl = s3Uploader.moveFolder(sourceKey, destinationKey);
			photo.updatePhotoUrl(renewalUrl);
		}
	}

	private String extractFileName(String photoUrl) {
		int lastSeparateIndex = photoUrl.lastIndexOf("/");
		return photoUrl.substring(lastSeparateIndex + 1);
	}
}
