package com.tf4.photospot.post.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.request.SimplePostListRequest;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostService {

	@Transactional
	public void upload(PostUploadRequest request) {

		// 이미지 좌표-> 주소로 변환 (1)
		// 주소로 다시 좌표 검색 (2)

		// 스팟 생성 별개
		// 스팟 조회 -> 없을 경우 -> 카카오 좌표 주소 변환 API (2번)
		// 방명록 생성
	}

	public void getSimplePosts(SimplePostListRequest request) {
		//
	}
}
