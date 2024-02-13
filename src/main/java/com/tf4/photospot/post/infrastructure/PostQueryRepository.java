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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
			.join(post.photo, photo)
			.where(createPostSearchBuilder(cond));
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
			.leftJoin(postLike).on(postLike.post.eq(post).and(equalsPostLike(cond.userId())))
			.where(createPostSearchBuilder(cond));
		return orderBy(query, post, pageable).toSlice(query, pageable);
	}

	public List<PostTag> findPostTagsIn(List<Post> posts) {
		return queryFactory.select(postTag)
			.from(postTag)
			.join(postTag.tag).fetchJoin()
			.where(postTag.post.in(posts))
			.fetch();
	}

	private BooleanBuilder createPostSearchBuilder(PostSearchCondition cond) {
		final BooleanBuilder searchBuilder = new BooleanBuilder(isNotDeleted());
		switch (cond.type()) {
			case MY_POSTS -> searchBuilder.and(equalsWriter(cond.userId()));
			case POSTS_OF_SPOT -> searchBuilder.and(equalsSpot(cond.spotId())).and(canVisible(cond));
		}
		return searchBuilder;
	}

	private BooleanExpression canVisible(PostSearchCondition cond) {
		return isPublicPost().or(equalsWriter(cond.userId()));
	}

	private static BooleanExpression isPublicPost() {
		return post.isPrivate.isFalse();
	}

	private static BooleanExpression isNotDeleted() {
		return post.deletedAt.isNull();
	}

	private BooleanBuilder equalsPostLike(Long userId) {
		return nullSafeBuilder(() -> postLike.user.id.eq(userId));
	}

	private BooleanBuilder equalsWriter(Long writerId) {
		return nullSafeBuilder(() -> post.writer.id.eq(writerId));
	}

	private BooleanBuilder equalsSpot(Long spotId) {
		return nullSafeBuilder(() -> post.spot.id.eq(spotId));
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
