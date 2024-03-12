package com.tf4.photospot.post.application.request;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.exception.domain.CommonErrorCode;

class PostSearchConditionTest {
	@TestFactory
	Stream<DynamicTest> postSearchCondTest() {
		//given
		final List<Sort> sortableList = List.of(
			Sort.by(Sort.Order.desc("id")),
			Sort.by(Sort.Order.desc("likeCount")),
			Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("id"))
		);
		final Sort unsortable = Sort.by(Sort.Order.desc("detailAddress"));
		final PageRequest defaultPageRequest = PageRequest.of(0, 10,
			Sort.by(Sort.Direction.DESC, "id"));
		return Stream.of(
			dynamicTest("특정 스팟의 방명록 조회 조건은 spotId가 필수다.", () -> {
				var postsOfSpotBuilder = PostSearchCondition.builder()
					.spotId(1L)
					.userId(1L)
					.pageable(defaultPageRequest)
					.type(PostSearchType.POSTS_OF_SPOT);
				var postsOfSpotBuilderNotSpotId = PostSearchCondition.builder()
					.pageable(defaultPageRequest)
					.type(PostSearchType.POSTS_OF_SPOT);
				assertThatNoException().isThrownBy(postsOfSpotBuilder::build);
				assertThatException().isThrownBy(postsOfSpotBuilderNotSpotId::build);
			}),
			dynamicTest("내 방명록 조회 조건은 userId가 필수다.", () -> {
				var myPostsBuilder = PostSearchCondition.builder()
					.userId(1L)
					.pageable(defaultPageRequest)
					.type(PostSearchType.MY_POSTS);
				var myPostsBuilderNotUserId = PostSearchCondition.builder()
					.pageable(defaultPageRequest)
					.type(PostSearchType.MY_POSTS);
				assertThatNoException().isThrownBy(myPostsBuilder::build);
				assertThatException().isThrownBy(myPostsBuilderNotUserId::build);
			}),
			dynamicTest("정렬 가능한 property로 페이징 요청을 생성할 수 있다.",
				() -> assertThatStream(sortableList.stream()).allSatisfy(sort ->
					assertThatNoException().isThrownBy(() -> PostSearchCondition.builder()
						.spotId(1L)
						.userId(1L)
						.pageable(PageRequest.of(0, 10, sort))
						.type(PostSearchType.POSTS_OF_SPOT)
						.build()))
			),
			dynamicTest("정렬 불가능한 property로 페이징 요청을 생성하면 INVALID_SEARCH_CONDITION 예외가 발생한다.",
				() -> assertThatException().isThrownBy(() ->
						PostSearchCondition.builder()
							.spotId(1L)
							.userId(1L)
							.pageable(PageRequest.of(0, 10, unsortable))
							.type(PostSearchType.POSTS_OF_SPOT)
							.build())
					.extracting("errorCode")
					.isEqualTo(CommonErrorCode.INVALID_SEARCH_CONDITION)
			),
			dynamicTest("내가 좋아요한 방명록 조회 조건은 userId가 필수다.", () -> {
				var likePostsBuilder = PostSearchCondition.builder()
					.userId(1L)
					.pageable(defaultPageRequest)
					.type(PostSearchType.LIKE_POSTS);
				var likePostsBuilderNotUserId = PostSearchCondition.builder()
					.pageable(defaultPageRequest)
					.type(PostSearchType.LIKE_POSTS);
				assertThatNoException().isThrownBy(likePostsBuilder::build);
				assertThatException().isThrownBy(likePostsBuilderNotUserId::build);
			}),
			dynamicTest("내가 좋아요한 방명록 조회 조건은 시간순 정렬만 가능하다.", () -> {
				final PageRequest pageRequestSortLikeCount = PageRequest.of(0, 10,
					Sort.by(Sort.Direction.DESC, "likeCount"));
				var likePostsBuilder = PostSearchCondition.builder()
					.userId(1L)
					.pageable(defaultPageRequest)
					.type(PostSearchType.LIKE_POSTS);
				var likePostsBuilderSortLikeCount = PostSearchCondition.builder()
					.userId(1L)
					.pageable(pageRequestSortLikeCount)
					.type(PostSearchType.LIKE_POSTS);
				assertThatNoException().isThrownBy(likePostsBuilder::build);
				assertThatException().isThrownBy(likePostsBuilderSortLikeCount::build);
			}),
			dynamicTest("앨범 방명록 조회 조건은 userId, albumId가 필수다.", () -> {
				var albumPostsBuilder = PostSearchCondition.builder()
					.userId(1L)
					.albumId(1L)
					.pageable(defaultPageRequest)
					.type(PostSearchType.ALBUM_POSTS);
				var albumPostsBuilderNotUserId = PostSearchCondition.builder()
					.pageable(defaultPageRequest)
					.albumId(1L)
					.type(PostSearchType.ALBUM_POSTS);
				var albumPostsBuilderNotAlbumId = PostSearchCondition.builder()
					.pageable(defaultPageRequest)
					.userId(1L)
					.type(PostSearchType.ALBUM_POSTS);
				assertThatNoException().isThrownBy(albumPostsBuilder::build);
				assertThatException().isThrownBy(albumPostsBuilderNotAlbumId::build);
				assertThatException().isThrownBy(albumPostsBuilderNotUserId::build);
			}),
			dynamicTest("앨범 방명록 조회 조건은 시간순 정렬만 가능하다.", () -> {
				final PageRequest pageRequestSortLikeCount = PageRequest.of(0, 10,
					Sort.by(Sort.Direction.DESC, "likeCount"));
				var albumPostsBuilder = PostSearchCondition.builder()
					.userId(1L)
					.albumId(1L)
					.pageable(defaultPageRequest)
					.type(PostSearchType.ALBUM_POSTS);
				var albumPostsBuilderSortLikeCount = PostSearchCondition.builder()
					.userId(1L)
					.albumId(1L)
					.pageable(pageRequestSortLikeCount)
					.type(PostSearchType.ALBUM_POSTS);
				assertThatNoException().isThrownBy(albumPostsBuilder::build);
				assertThatException().isThrownBy(albumPostsBuilderSortLikeCount::build);
			})
		);
	}
}
