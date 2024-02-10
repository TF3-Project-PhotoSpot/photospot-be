package com.tf4.photospot.post.infrastructure;

import static com.tf4.photospot.photo.domain.QBubble.*;
import static com.tf4.photospot.photo.domain.QPhoto.*;
import static com.tf4.photospot.post.domain.QPost.*;
import static com.tf4.photospot.post.domain.QPostLike.*;
import static com.tf4.photospot.post.domain.QPostTag.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.util.PageUtils;
import com.tf4.photospot.global.util.QueryDslUtils;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostPreviewListRequest;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.application.response.QPostPreviewResponse;
import com.tf4.photospot.post.application.response.QPostWithLikeStatus;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.user.domain.QUser;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Slice<PostPreviewResponse> findPostPreviews(PostPreviewListRequest request) {
		final Pageable pageable = request.pageable();
		var query = queryFactory.select(new QPostPreviewResponse(
				post.spot.id,
				post.id,
				photo.photoUrl
			))
			.from(post)
			.join(post.photo, photo)
			.where(
				post.spot.id.eq(request.spotId()),
				canVisble()
			);
		return PageUtils.toSlice(pageable, QueryDslUtils.orderBy(query, post, pageable)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1L)
			.fetch());
	}

	public Slice<PostWithLikeStatus> findPostsWithLikeStatus(PostListRequest request) {
		final Pageable pageable = request.pageable();
		final QUser writer = new QUser("writer");
		var query = queryFactory.select(new QPostWithLikeStatus(post, postLike.isNotNull().as("isLiked")))
			.from(post)
			.join(post.writer, writer).fetchJoin()
			.join(post.photo, photo).fetchJoin()
			.leftJoin(photo.bubble, bubble).fetchJoin()
			.leftJoin(postLike).on(postLike.post.eq(post).and(postLike.user.id.eq(request.userId())))
			.where(
				post.spot.id.eq(request.spotId()),
				canVisble()
			);
		return PageUtils.toSlice(pageable, QueryDslUtils.orderBy(query, post, pageable)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1L)
			.fetch());
	}

	public List<PostTag> findPostTagsIn(List<Post> posts) {
		return queryFactory.select(postTag)
			.from(postTag)
			.join(postTag.tag).fetchJoin()
			.where(postTag.post.in(posts))
			.fetch();
	}

	private BooleanExpression canVisble() {
		return post.isPrivate.isFalse().and(post.deletedAt.isNull());
	}

	public boolean existsPostLike(Post post, User user) {
		final Integer exists = queryFactory.selectOne()
			.from(postLike)
			.where(postLike.post.eq(post).and(postLike.user.eq(user)))
			.fetchFirst();
		return exists != null;
	}
}
