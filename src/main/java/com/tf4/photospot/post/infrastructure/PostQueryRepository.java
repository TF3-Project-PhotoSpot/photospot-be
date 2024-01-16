package com.tf4.photospot.post.infrastructure;

import static com.tf4.photospot.photo.domain.QPhoto.*;
import static com.tf4.photospot.post.domain.QPost.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.QPostPreviewResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {
	private final JPAQueryFactory queryFactory;

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
