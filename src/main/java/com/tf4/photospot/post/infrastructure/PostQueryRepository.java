package com.tf4.photospot.post.infrastructure;

import static com.tf4.photospot.photo.domain.QBubble.*;
import static com.tf4.photospot.photo.domain.QPhoto.*;
import static com.tf4.photospot.post.domain.QPost.*;
import static com.tf4.photospot.post.domain.QPostLike.*;
import static com.tf4.photospot.post.domain.QPostTag.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.util.QueryDslUtils;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostWithLikeStatus;
import com.tf4.photospot.post.application.response.QPostPreviewResponse;
import com.tf4.photospot.post.application.response.QPostWithLikeStatus;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.user.domain.QUser;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	public Slice<PostPreviewResponse> findPostPreviews(PostSearchCondition cond) {
		final Pageable pageable = cond.pageable();
		var query = queryFactory.select(new QPostPreviewResponse(
				post.spot.id,
				post.id,
				photo.photoUrl
			))
			.from(post)
			.join(post.photo, photo);
		applyPostPreviewCondition(query, cond);
		return orderBy(query, post, pageable).toSlice(query, pageable);
	}

	public Slice<PostWithLikeStatus> findPostsWithLikeStatus(PostSearchCondition cond) {
		final Pageable pageable = cond.pageable();
		final QUser writer = new QUser("writer");
		var query = queryFactory.select(new QPostWithLikeStatus(post, postLike.isNotNull()))
			.from(post)
			.join(post.writer, writer).fetchJoin()
			.join(post.photo, photo).fetchJoin()
			.leftJoin(photo.bubble, bubble).fetchJoin()
			.leftJoin(postLike).on(postLike.post.eq(post).and(equalsPostLike(cond.userId())));
		applyPostPreviewCondition(query, cond);
		return orderBy(query, post, pageable).toSlice(query, pageable);
	}

	public List<PostTag> findPostTagsIn(List<Post> posts) {
		return queryFactory.select(postTag)
			.from(postTag)
			.join(postTag.tag).fetchJoin()
			.where(postTag.post.in(posts))
			.fetch();
	}

	private <T> void applyPostPreviewCondition(JPAQuery<T> query, PostSearchCondition cond) {
		switch (cond.type()) {
			case MY_POSTS -> query.where(
				equalsWriter(cond.userId()),
				post.deletedAt.isNull()
			);
			case POSTS_OF_SPOT -> query.where(
				equalsSpot(cond.spotId()),
				post.isPrivate.isFalse().or(equalsWriter(cond.userId())),
				post.deletedAt.isNull()
			);
		}
	}

	private BooleanExpression equalsPostLike(Long userId) {
		if (userId == null) {
			return null;
		}
		return postLike.user.id.eq(userId);
	}

	private BooleanExpression equalsWriter(Long writerId) {
		if (writerId == null) {
			return null;
		}
		return post.writer.id.eq(writerId);
	}

	private BooleanExpression equalsSpot(Long spotId) {
		if (spotId == null) {
			return null;
		}
		return post.spot.id.eq(spotId);
	}

	public boolean existsPostLike(Post post, User user) {
		final Integer exists = queryFactory.selectOne()
			.from(postLike)
			.where(postLike.post.eq(post).and(postLike.user.eq(user)))
			.fetchFirst();
		return exists != null;
	}

	public Optional<PostLike> findPostLikeFetch(Long postId, Long userId) {
		return Optional.ofNullable(queryFactory.selectFrom(postLike)
			.join(postLike.post, post).fetchJoin()
			.where(postLike.post.id.eq(postId).and(postLike.user.id.eq(userId)))
			.fetchOne());
	}
}
