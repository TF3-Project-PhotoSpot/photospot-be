package com.tf4.photospot.photo.application;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PhotoScheduler {

	private final PhotoService photoService;

	// Todo : 삭제 스케줄링
	@Scheduled(cron = "0 0 4 * * *")
	public void scheduleToMovePhotos() {
		photoService.movePhotos();
	}
}
