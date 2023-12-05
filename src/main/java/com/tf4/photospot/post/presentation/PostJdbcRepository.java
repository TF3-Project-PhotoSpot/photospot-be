package com.tf4.photospot.post.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tf4.photospot.post.application.response.PostThumbnailResponse;
import com.tf4.photospot.post.application.response.PostThumbnailsResponse;

@Repository
public class PostJdbcRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public PostJdbcRepository(DataSource dataSource) {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	/*
	 *	스팟별로 파티션을 나눠서 최신 방명록을 postThumbnailCount만큼 필터링합니다.
	 * 	필터링이 된 방명록의 미리보기 정보(사진)를 가져와서 스팟별로 최신 방명록 미리보기를 분류합니다.
	 * */
	public PostThumbnailsResponse findRecentlyPostThumbnailsInSpotIds(List<Long> spotIds, int postThumbnailCount) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("spotIds", spotIds)
			.addValue("postThumbnailCount", postThumbnailCount);

		Map<Long, List<PostThumbnailResponse>> response = spotIds.stream()
			.collect(Collectors.toMap(spotId -> spotId, spotId -> new ArrayList<>()));

		jdbcTemplate.query("""
			select spot_id, recently_posts.id, photo_url
			from (
				select id, spot_id, photo_id
				from (
					select id, spot_id, photo_id,
						row_number() over(partition by spot_id order by id desc) as recently_post_rank
					from post
					where spot_id in (:spotIds) and is_private = false and deleted_at is null
				) as posts_by_spot
				where recently_post_rank <= :postThumbnailCount
			) as recently_posts
			join photo on photo_id = photo.id
			""", params, (rs, rowNum) ->
			response.get(rs.getLong("spot_id"))
				.add(new PostThumbnailResponse(
					rs.getLong("id"),
					rs.getString("photo_url")))
		);

		return new PostThumbnailsResponse(response);
	}
}

