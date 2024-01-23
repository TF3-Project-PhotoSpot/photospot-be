package com.tf4.photospot.post.infrastructure;

import static com.querydsl.core.group.GroupBy.*;
import static com.tf4.photospot.photo.domain.QBubble.*;
import static com.tf4.photospot.photo.domain.QPhoto.*;
import static com.tf4.photospot.post.domain.QPost.*;
import static com.tf4.photospot.post.domain.QPostLike.*;
import static com.tf4.photospot.post.domain.QPostTag.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.util.PageUtils;
import com.tf4.photospot.global.util.QueryDslUtils;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostWithLikedResponse;
import com.tf4.photospot.post.application.response.QPostPreviewResponse;
import com.tf4.photospot.post.application.response.QPostWithLikedResponse;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.user.domain.QUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Slice<PostDetailResponse> findPosts(Long spotId, Long readUserId, Pageable pageable) {
		final QUser writer = new QUser("writer");
		var query = queryFactory.select(new QPostWithLikedResponse(post, postLike.isNotNull().as("isLiked")))
			.from(post)
			.join(post.writer, writer).fetchJoin()
			.join(post.photo, photo).fetchJoin()
			.leftJoin(photo.bubble, bubble).fetchJoin()
			.leftJoin(postLike).on(postLike.post.eq(post).and(postLike.user.id.eq(readUserId)))
			.where(
				post.spot.id.eq(spotId),
				canVisble()
			);
		final List<PostWithLikedResponse> postResponses = QueryDslUtils.orderBy(query, post, pageable)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		final Map<Long, List<PostTag>> postTagsByPostId = queryFactory.select(postTag)
			.from(postTag)
			.join(postTag.tag).fetchJoin()
			.where(postTag.post.in(postResponses.stream().map(PostWithLikedResponse::post).toList()))
			.transform(groupBy(postTag.post.id).as(list(postTag)));

		final List<PostDetailResponse> postDetailResponses = postResponses.stream()
			.map(postResponse -> PostDetailResponse.of(postResponse,
				postTagsByPostId.getOrDefault(postResponse.post().getId(), Collections.emptyList())))
			.toList();

		return PageUtils.toSlice(pageable, postDetailResponses);
	}

	public List<PostPreviewResponse> findRecentlyPostPreviews(Long spotId, int postPreviewCount) {
		return queryFactory.select(new QPostPreviewResponse(
				post.spot.id,
				post.id,
				photo.photoUrl
			))
			.from(post)
			.join(post.photo, photo)
			.where(
				post.spot.id.eq(spotId),
				canVisble()
			)
			.orderBy(post.id.desc())
			.limit(postPreviewCount)
			.fetch();
	}

	private BooleanExpression canVisble() {
		return post.isPrivate.isFalse().and(post.deletedAt.isNull());
	}
}
