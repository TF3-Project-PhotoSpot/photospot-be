package com.tf4.photospot.post.infrastructure;

import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.spot.application.response.MostPostTagRank;
import com.tf4.photospot.spot.domain.Spot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	public List<PostPreviewResponse> findRecentPostPreviewsInSpots(List<Spot> spots, int postPreviewCount) {
		if (spots.isEmpty()) {
			return Collections.emptyList();
		}
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("spotIds", spots.stream().map(Spot::getId).toList())
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

	public boolean savePostTags(Long postId, Long spotId, List<Long> tags) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("postId", postId)
			.addValue("spotId", spotId)
			.addValue("tags", tags);
		int rowNum = jdbcTemplate.update("""
			insert into post_tag (post_id, spot_id, tag_id)
			select :postId, :spotId, id from tag where id in (:tags)
			""", params);
		return rowNum == tags.size();
	}

	public boolean saveMentions(Long postId, List<Long> mentionedUsers) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("postId", postId)
			.addValue("mentionedUsers", mentionedUsers);
		int rowNum = jdbcTemplate.update("""
			insert into mention (post_id, user_id)
			select :postId, id from users where id in (:mentionedUsers)
			""", params);
		return rowNum == mentionedUsers.size();
	}

	public void deletePostTagsByPostId(Long postId) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("postId", postId);
		jdbcTemplate.update("delete from post_tag where post_id = :postId", params);
	}

	public void deleteMentionsByPostId(Long postId) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("postId", postId);
		jdbcTemplate.update("delete from mention where post_id = :postId", params);
	}

	public List<MostPostTagRank> findMostPostTagsOfSpot(Spot spot, int count) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("spotId", spot.getId())
			.addValue("count", count);
		return jdbcTemplate.query("""
			select t.id, tag_count, t.name, t.icon_url
			from (
				select tag_id, count(id) as tag_count
				from post_tag
				where spot_id = :spotId
				group by tag_id
				order by tag_count desc
				limit :count) as pt
			join tag t on pt.tag_id = t.id
			""", params, ((rs, rowNum) -> MostPostTagRank.builder()
			.id(rs.getLong("id"))
			.count(rs.getInt("tag_count"))
			.name(rs.getString("name"))
			.iconUrl(rs.getString("icon_url"))
			.build()));
	}

	public void increasePostLike(Long postId) {
		jdbcTemplate.update("update post set like_count = like_count + 1 where id = :id",
			new MapSqlParameterSource("id", postId));
	}

	public void decreasePostLike(Long postId) {
		try {
			jdbcTemplate.update("update post set like_count = like_count - 1 where id = :id",
				new MapSqlParameterSource("id", postId));
		} catch (DataIntegrityViolationException exception) {
			log.error("[POST ID={}] 방명록의 좋아요 개수가 일치하지 않습니다.", postId);
			syncLikeCount(postId);
		}
	}

	private void syncLikeCount(Long postId) {
		jdbcTemplate.update("""
				update post
				set like_count = (
					select count(*)
					from post_like pl
					where pl.post_id = :post_id
				)
				where id = :post_id
					""",
			new MapSqlParameterSource("post_id", postId));
	}
}
