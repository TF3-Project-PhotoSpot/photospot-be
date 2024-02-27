package com.tf4.photospot.album.infrastructure;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

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
}
