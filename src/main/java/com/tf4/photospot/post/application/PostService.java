package com.tf4.photospot.post.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.infrastructure.PostQueryRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostService {
	private final PostQueryRepository postQueryRepository;

	@Transactional
	public void upload(PostUploadRequest request) {
		// 이미지 좌표-> 주소로 변환 (1)
		// 주소로 다시 좌표 검색 (2)

		// 스팟 생성 별개
		// 스팟 조회 -> 없을 경우 -> 카카오 좌표 주소 변환 API (2번)
		// 방명록 생성
	}

	public SlicePageDto<PostDetailResponse> getPosts(PostListRequest request) {
		final Slice<PostWithLikeStatus> postResponses = postQueryRepository.findPostsWithLikeStatus(request);
		final Map<Post, List<PostTag>> postTagGroup = postQueryRepository
			.findPostTagsIn(postResponses.stream().map(PostWithLikeStatus::post).toList())
			.stream()
			.collect(Collectors.groupingBy(PostTag::getPost));
		final List<PostDetailResponse> postDetailResponses = postResponses.stream()
			.map(postResponse -> PostDetailResponse.of(postResponse,
				postTagGroup.getOrDefault(postResponse.post(), Collections.emptyList())))
			.toList();
		return SlicePageDto.wrap(postDetailResponses, postResponses.hasNext());
	}

	public SlicePageDto<PostDetailResponse> getPostPreviews(PostListRequest request) {
		final Slice<PostWithLikeStatus> postResponses = postQueryRepository.findPostsWithLikeStatus(request);
		final Map<Post, List<PostTag>> postTagGroup = postQueryRepository
			.findPostTagsIn(postResponses.stream().map(PostWithLikeStatus::post).toList())
			.stream()
			.collect(Collectors.groupingBy(PostTag::getPost));
		final List<PostDetailResponse> postDetailResponses = postResponses.stream()
			.map(postResponse -> PostDetailResponse.of(postResponse,
				postTagGroup.getOrDefault(postResponse.post(), Collections.emptyList())))
			.toList();
		return SlicePageDto.wrap(postDetailResponses, postResponses.hasNext());
	}
}
