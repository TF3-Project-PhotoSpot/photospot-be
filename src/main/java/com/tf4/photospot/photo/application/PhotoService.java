package com.tf4.photospot.photo.application;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.photo.application.response.PhotoUploadResponse;
import com.tf4.photospot.photo.domain.S3Directory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

	private final S3Uploader s3Uploader;

	public PhotoUploadResponse upload(MultipartFile file) {
		return new PhotoUploadResponse(s3Uploader.upload(file, S3Directory.TEMP_FOLDER.getFolder()));
	}
}
