package com.tf4.photospot.album.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.album.application.response.AlbumPreviewResponse;
import com.tf4.photospot.album.application.response.CreateAlbumPostResponse;
import com.tf4.photospot.album.domain.Album;
import com.tf4.photospot.album.domain.AlbumRepository;
import com.tf4.photospot.album.domain.AlbumUser;
import com.tf4.photospot.album.domain.AlbumUserRepository;
import com.tf4.photospot.album.infrastructure.AlbumJdbcRepository;
import com.tf4.photospot.album.infrastructure.AlbumQueryRepository;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AlbumErrorCode;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.user.application.UserService;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlbumService {
	private final PostService postService;
	private final AlbumRepository albumRepository;
	private final AlbumUserRepository albumUserRepository;
	private final AlbumQueryRepository albumQueryRepository;
	private final AlbumJdbcRepository albumJdbcRepository;
	private final UserService userService;

	public SlicePageDto<PostPreviewResponse> getPostPreviewsOfAlbum(PostSearchCondition postSearchCond) {
		validateAlbumUser(postSearchCond.albumId(), postSearchCond.userId());
		return postService.getPostPreviews(postSearchCond);
	}

	public SlicePageDto<PostDetailResponse> getPostsOfAlbum(PostSearchCondition postSearchCond) {
		validateAlbumUser(postSearchCond.albumId(), postSearchCond.userId());
		return postService.getPosts(postSearchCond);
	}

	@Transactional
	public Long create(Long userId, String albumName) {
		final User user = userService.getActiveUser(userId);
		final Album album = new Album(albumName);
		albumRepository.save(album);
		albumUserRepository.save(new AlbumUser(user, album));
		return album.getId();
	}

	@Transactional
	public List<CreateAlbumPostResponse> addPosts(List<Long> postIds, Long albumId, Long userId) {
		validateAlbumUser(albumId, userId);
		final List<Long> existingPostIds = albumQueryRepository.findPostIdsOfAlbumPosts(albumId, postIds);
		final List<Long> addablePostIds = new ArrayList<>(postIds);
		addablePostIds.removeAll(existingPostIds);
		albumJdbcRepository.saveAlbumPosts(albumId, addablePostIds);
		return addablePostIds.stream().map(CreateAlbumPostResponse::new).toList();
	}

	@Transactional
	public void replacePosts(List<Long> postIds, Long albumId, Long userId) {
		validateAlbumUser(albumId, userId);
		removePosts(postIds, albumId, userId);
		albumJdbcRepository.saveAlbumPosts(albumId, postIds);
	}

	@Transactional
	public void removePosts(List<Long> postIds, Long albumId, Long userId) {
		validateAlbumUser(albumId, userId);
		Long removedAlbumPostCount = albumQueryRepository.removeAlbumPosts(postIds, albumId);
		if (removedAlbumPostCount != postIds.size()) {
			throw new ApiException(AlbumErrorCode.CONTAINS_ALBUM_POSTS_CANNOT_DELETE);
		}
	}

	@Transactional
	public void remove(Long albumId, Long userId) {
		validateAlbumUser(albumId, userId);
		albumQueryRepository.removeAlbum(albumId);
	}

	private void validateAlbumUser(Long albumId, Long userId) {
		if (!albumQueryRepository.exixtsUserAlbum(userId, albumId)) {
			throw new ApiException(AlbumErrorCode.NO_AUTHORITY_ALBUM);
		}
	}

	public List<AlbumPreviewResponse> getAlbums(Long userId) {
		return albumJdbcRepository.getAlbumPreviews(userId);
	}
}
