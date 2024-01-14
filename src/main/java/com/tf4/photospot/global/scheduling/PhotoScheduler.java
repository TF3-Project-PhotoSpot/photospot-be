package com.tf4.photospot.global.scheduling;

import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.post.domain.PostRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PhotoScheduler {

	private final PhotoRepository photoRepository;
	private final PostRepository postRepository;
	private final AmazonS3 amazonS3Client;

}
