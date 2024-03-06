package com.tf4.photospot.album.infrastructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.album.application.response.AlbumPreviewResponse;

@Repository
public class AlbumJdbcRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public AlbumJdbcRepository(DataSource dataSource) {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public void saveAlbumPosts(Long albumId, List<Long> postIds) {
		final SqlParameterSource[] params = postIds.stream()
			.map(postId -> new MapSqlParameterSource()
				.addValue("albumId", albumId)
				.addValue("postId", postId))
			.toList()
			.toArray(new MapSqlParameterSource[0]);
		jdbcTemplate.batchUpdate("insert into album_post (album_id, post_id) values (:albumId, :postId)", params);
	}

	public List<AlbumPreviewResponse> getAlbumPreviews(Long userId) {
		final List<AlbumPreviewResponse> albumPreviews = findAllAlbumPreviews(userId);
		if (albumPreviews.isEmpty()) {
			return Collections.emptyList();
		}
		final List<Long> albumIds = albumPreviews.stream().map(AlbumPreviewResponse::albumId).toList();
		final Map<Long, AlbumPreviewResponse> albumPreviewsWithImage = new HashMap<>();
		jdbcTemplate.query("""
				select album.id, album.name, photo.photo_url
				from (
					select sub_ap.album_id, max(sub_ap.id) as latest_album_post
					from album_post as sub_ap
					join post on sub_ap.post_id = post.id
					left join album_user au on sub_ap.album_id = au.album_id and au.user_id = post.user_id
					where sub_ap.album_id in (:albumIds)
						and post.deleted_at is null
						and (post.is_private is false or au.id is not null)
					group by sub_ap.album_id
				) as ap
				join album_post on album_post.id = latest_album_post
				join album on album.id = album_post.album_id
				join post on post.id = album_post.post_id
				join photo on photo.id = post.photo_id
				""", new MapSqlParameterSource("albumIds", albumIds),
			(rs, row) -> albumPreviewsWithImage.put(rs.getLong("id"), AlbumPreviewResponse.builder()
				.albumId(rs.getLong("id"))
				.name(rs.getString("name"))
				.photoUrl(rs.getString("photo_url"))
				.build()
			));
		return albumPreviews.stream()
			.map(albumPreview -> albumPreviewsWithImage.getOrDefault(albumPreview.albumId(), albumPreview))
			.toList();
	}

	private List<AlbumPreviewResponse> findAllAlbumPreviews(Long userId) {
		return jdbcTemplate.query("""
						select album.id, album.name
						from album
						join album_user on album.id = album_user.album_id
						where album_user.user_id = :userId
						order by album.id
			""", new MapSqlParameterSource("userId", userId), (rs, row) -> AlbumPreviewResponse.builder()
			.albumId(rs.getLong("id"))
			.name(rs.getString("name"))
			.build());
	}
}
