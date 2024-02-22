package com.tf4.photospot.album.infrastructure;

import static com.tf4.photospot.album.domain.QAlbum.*;
import static com.tf4.photospot.album.domain.QAlbumPost.*;
import static com.tf4.photospot.album.domain.QAlbumUser.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.album.domain.AlbumUser;
import com.tf4.photospot.global.util.QueryDslUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AlbumQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	public boolean exixtsUserAlbum(Long userId, Long albumId) {
		final Integer exists = queryFactory.selectOne()
			.from(albumUser)
			.where(albumUser.user.id.eq(userId).and(albumUser.album.id.eq(albumId)))
			.fetchFirst();
		return exists != null;
	}

	public Optional<AlbumUser> findAlbumUser(Long albumId, Long userId) {
		return Optional.ofNullable(queryFactory.selectFrom(albumUser)
			.join(albumUser.album, album).fetchJoin()
			.where(albumUser.album.id.eq(albumId).and(albumUser.user.id.eq(userId)))
			.fetchFirst());
	}

	public List<Long> findPostIdsOfAlbumPosts(Long albumId, List<Long> postIds) {
		return queryFactory.select(albumPost.post.id)
			.from(albumPost)
			.where(albumPost.album.id.eq(albumId).and(albumPost.post.id.in(postIds)))
			.fetch();
	}

	public void removeAlbum(Long albumId) {
		queryFactory.delete(albumPost)
			.where(albumPost.album.id.eq(albumId))
			.execute();
		queryFactory.delete(albumUser)
			.where(albumUser.album.id.eq(albumId))
			.execute();
		queryFactory.delete(album)
			.where(album.id.eq(albumId))
			.execute();
	}

	public Long removeAlbumPosts(List<Long> postIds, Long albumId) {
		return queryFactory.delete(albumPost)
			.where(albumPost.album.id.eq(albumId).and(albumPost.post.id.in(postIds)))
			.execute();
	}
}
