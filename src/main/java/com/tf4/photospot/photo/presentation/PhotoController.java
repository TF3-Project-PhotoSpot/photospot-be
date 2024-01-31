package com.tf4.photospot.photo.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.photo.application.PhotoService;
import com.tf4.photospot.photo.application.request.PhotoSaveRequest;
import com.tf4.photospot.photo.application.response.PhotoSaveResponse;
import com.tf4.photospot.photo.application.response.PhotoUploadResponse;
import com.tf4.photospot.photo.presentation.request.PhotoSaveHttpRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photos")
public class PhotoController {

	private final PhotoService photoService;

	@PostMapping("/s3")
	public PhotoUploadResponse uploadPhoto(@RequestPart("file") MultipartFile file) {
		return photoService.upload(file);
	}

	@PostMapping
	public PhotoSaveResponse savePhoto(@RequestBody PhotoSaveHttpRequest request) {
		return photoService.save(new PhotoSaveRequest(request.photoUrl(), request.coord().toCoord(), request.toDate()));
	}
}
