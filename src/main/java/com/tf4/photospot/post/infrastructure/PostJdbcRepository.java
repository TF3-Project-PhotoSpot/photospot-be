package com.tf4.photospot.post.infrastructure;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.post.application.response.PostPreviewResponse;

@Repository
public class PostJdbcRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public PostJdbcRepository(DataSource dataSource) {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	/*
	 *	스팟별로 파티션을 나눠서 최신 방명록을 postPreviewCount만큼 필터링합니다.
	 * 	필터링이 된 방명록의 미리보기 정보(사진)를 가져옵니다.
	 * */
	public List<PostPreviewResponse> findRecentlyPostThumbnailsInSpotIds(List<Long> spotIds, int postPreviewCount) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("spotIds", spotIds)
			.addValue("postPreviewCount", postPreviewCount);
		return jdbcTemplate.query("""
			select post.id as post_id, spot_id, photo_url
			from (
				select id, spot_id, photo_id,
				row_number() over(partition by spot_id order by id desc) as recently_post
				from post p
				where spot_id in (:spotIds) and is_private = false and deleted_at is null
			) as post
			join photo on photo_id = photo.id
			where recently_post <= :postPreviewCount
			""", params, (rs, rowNum) -> new PostPreviewResponse(
			rs.getLong("spot_id"),
			rs.getLong("post_id"),
			rs.getString("photo_url"))
		);
	}

	public int savePostTags(Long postId, Long spotId, List<Long> tags) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("postId", postId)
			.addValue("spotId", spotId)
			.addValue("tags", tags);
		return jdbcTemplate.update("""
			insert into post_tag (post_id, spot_id, tag_id)
			select :postId, :spotId, id from tag where id in (:tags)
			""", params);
	}

	public int saveMentions(Long postId, List<Long> mentionedUsers) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("postId", postId)
			.addValue("mentionedUsers", mentionedUsers);
		return jdbcTemplate.update("""
			insert into mention (post_id, user_id)
			select :postId, id from users where id in (:mentionedUsers)
			""", params);
	}
}

