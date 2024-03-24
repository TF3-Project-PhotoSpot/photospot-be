package com.tf4.photospot.post.infrastructure;

import static com.tf4.photospot.album.domain.QAlbumPost.*;
import static com.tf4.photospot.album.domain.QAlbumUser.*;
import static com.tf4.photospot.photo.domain.QBubble.*;
import static com.tf4.photospot.photo.domain.QPhoto.*;
import static com.tf4.photospot.post.domain.QMention.*;
import static com.tf4.photospot.post.domain.QPost.*;
import static com.tf4.photospot.post.domain.QPostLike.*;
import static com.tf4.photospot.post.domain.QPostTag.*;
import static com.tf4.photospot.post.domain.QReport.*;
import static com.tf4.photospot.spot.domain.QSpot.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.global.util.QueryDslUtils;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.application.response.PostDetail;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.QPostDetail;
import com.tf4.photospot.post.application.response.QPostPreviewResponse;
import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.post.domain.TagRepository;
import com.tf4.photospot.user.domain.QUser;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;
	private final TagRepository tagRepository;

	public Slice<PostPreviewResponse> findPostPreviews(PostSearchCondition cond) {
		final PostSearchType searchType = cond.type();
		final Pageable pageable = cond.pageable();
		var query = queryFactory.select(new QPostPreviewResponse(
				post.spot.id,
				post.id,
				photo.photoUrl
			))
			.from(post)
			.join(post.photo, photo)
			.leftJoin(report).on(report.post.eq(post).and(report.reporter.id.eq(cond.userId())));
		if (searchType == PostSearchType.LIKE_POSTS) {
			query.join(postLike).on(postLike.post.eq(post));
		}
		if (searchType == PostSearchType.ALBUM_POSTS) {
			query.join(albumPost).on(albumPost.post.eq(post))
				.leftJoin(albumUser).on(albumUser.user.eq(post.writer));
		}
		query.where(createPostSearchBuilder(cond));
		return orderBy(query, getPostSearchPathBase(cond.type()), pageable).toSlice(query, pageable);
	}

	public Slice<PostDetail> findPostDetails(PostSearchCondition cond) {
		final PostSearchType searchType = cond.type();
		final Pageable pageable = cond.pageable();
		final QUser writer = new QUser("writer");
		var query = queryFactory.select(new QPostDetail(
				post,
				spot.address,
				postLike.isNotNull()))
			.from(post)
			.join(post.spot, spot)
			.join(post.writer, writer).fetchJoin()
			.join(post.photo, photo).fetchJoin()
			.leftJoin(photo.bubble, bubble).fetchJoin()
			.leftJoin(report).on(report.post.eq(post).and(report.reporter.id.eq(cond.userId())));
		if (searchType == PostSearchType.LIKE_POSTS) {
			query.join(postLike).on(postLike.post.eq(post));
		} else {
			query.leftJoin(postLike).on(postLike.post.eq(post).and(equalsPostLike(cond.userId())));
		}
		if (searchType == PostSearchType.ALBUM_POSTS) {
			query.join(albumPost).on(albumPost.post.eq(post))
				.leftJoin(albumUser).on(albumUser.user.eq(post.writer));
		}
		query.where(createPostSearchBuilder(cond));
		return orderBy(query, getPostSearchPathBase(searchType), pageable).toSlice(query, pageable);
	}

	private EntityPathBase<? extends BaseEntity> getPostSearchPathBase(PostSearchType type) {
		if (type == PostSearchType.LIKE_POSTS) {
			return postLike;
		}
		if (type == PostSearchType.ALBUM_POSTS) {
			return albumPost;
		}
		return post;
	}

	public PostDetail findPost(Long userId, Long postId) {
		final QUser writer = new QUser("writer");
		return queryFactory.select(new QPostDetail(
				post,
				spot.address,
				postLike.isNotNull()))
			.from(post)
			.join(post.spot, spot)
			.join(post.writer, writer).fetchJoin()
			.join(post.photo, photo).fetchJoin()
			.leftJoin(photo.bubble, bubble).fetchJoin()
			.leftJoin(postLike).on(postLike.post.eq(post).and(equalsPostLike(userId)))
			.where(post.id.eq(postId).and(canVisible(userId)))
			.fetchOne();
	}

	public List<PostTag> findPostTagsIn(List<Post> posts) {
		return queryFactory.select(postTag)
			.from(postTag)
			.join(postTag.tag).fetchJoin()
			.where(postTag.post.in(posts))
			.fetch();
	}

	public List<Mention> findMentionsIn(List<Post> posts) {
		return queryFactory.select(mention)
			.from(mention)
			.join(mention.mentionedUser).fetchJoin()
			.where(mention.post.in(posts))
			.fetch();
	}

	private BooleanBuilder createPostSearchBuilder(PostSearchCondition cond) {
		final BooleanBuilder searchBuilder = new BooleanBuilder(isNotDeleted()).and(isNotReported());
		switch (cond.type()) {
			case MY_POSTS -> searchBuilder.and(equalsWriter(cond.userId()));
			case POSTS_OF_SPOT -> searchBuilder.and(equalsSpot(cond.spotId())).and(canVisible(cond.userId()));
			case LIKE_POSTS -> searchBuilder.and(equalsPostLike(cond.userId())).and(canVisible(cond.userId()));
			case ALBUM_POSTS -> searchBuilder.and(equalsAlbum(cond.albumId()))
				.and((isPublicPost())).or(albumUser.isNotNull());
		}
		return searchBuilder;
	}

	private BooleanExpression canVisible(Long userId) {
		return (isPublicPost()).or(equalsWriter(userId));
	}

	private static BooleanExpression isPublicPost() {
		return post.isPrivate.isFalse();
	}

	private static BooleanExpression isNotDeleted() {
		return post.deletedAt.isNull();
	}

	private static BooleanExpression isNotReported() {
		return report.id.isNull();
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

	private BooleanBuilder equalsAlbum(Long albumId) {
		return nullSafeBuilder(() -> albumPost.album.id.eq(albumId));
	}

	public boolean existsReport(Post post, User user) {
		final Integer exists = queryFactory.selectOne()
			.from(report)
			.where(report.post.eq(post).and(report.reporter.eq(user)))
			.fetchFirst();
		return exists != null;
	}

	public List<Tag> getTags() {
		return tagRepository.findAll();
	}

	public boolean cancelLike(Long postId, Long userId) {
		final long deleted = queryFactory.delete(postLike)
			.where(postLike.post.id.eq(postId).and(postLike.user.id.eq(userId)))
			.execute();
		return deleted != 0L;
	}
}
