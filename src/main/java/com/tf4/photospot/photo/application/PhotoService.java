package com.tf4.photospot.photo.application;

import java.time.LocalDate;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.global.util.S3Uploader;
import com.tf4.photospot.photo.domain.Directory;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.photo.presentation.response.PhotoSaveResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoService {

	private final PhotoRepository photoRepository;
	private final S3Uploader s3Uploader;

	@Transactional
	public PhotoSaveResponse save(MultipartFile file, Point point, LocalDate takenAt) {
		String photoUrl = s3Uploader.upload(file, Directory.POST_FOLDER.getType());
		Photo photo = Photo.builder()
			.photoUrl(photoUrl)
			.coord(point)
			.takenAt(takenAt)
			.build();
		Long postPhotoId = photoRepository.save(photo).getId();
		return new PhotoSaveResponse(postPhotoId);
	}

}
