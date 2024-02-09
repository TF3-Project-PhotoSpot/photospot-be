package com.tf4.photospot.photo.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tf4.photospot.photo.application.PhotoService;
import com.tf4.photospot.photo.application.response.PhotoUploadResponse;

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
}
