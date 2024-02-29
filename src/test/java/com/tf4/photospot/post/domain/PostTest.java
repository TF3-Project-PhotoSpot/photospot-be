package com.tf4.photospot.post.domain;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

public class PostTest {
	private User writer;
	private Spot spot;
	private Post post;
	private User otherUser;

	@BeforeEach
	void setUp() {
		Photo photo = createPhoto();
		writer = createUser("작성자");
		otherUser = createUser("다른 사용자");
		spot = createSpot();
		post = createPost(spot, writer, photo);
	}

	@TestFactory
	Stream<DynamicTest> delete() {
		return Stream.of(
			dynamicTest("방명록을 삭제한다.", () -> {
				// when
				post.delete(writer);

				// then
				assertNotNull(post.getDeletedAt());
			}),
			dynamicTest("다른 사람이 작성한 방명록을 삭제하면 예외를 던진다.", () -> {
				// when & then
				assertThatThrownBy(() -> post.delete(otherUser)).isInstanceOf(ApiException.class)
					.hasMessage(AuthErrorCode.PERMISSION_DENIED.getMessage());
			})
		);
	}

	@Test
	void likeFromTest() {
		//when
		final Long beforeLikeCount = post.getLikeCount();
		final PostLike postLike = post.likeFrom(otherUser);
		//then
		assertThat(postLike.getPost()).isEqualTo(post);
		assertThat(post.getLikeCount()).isEqualTo(beforeLikeCount + 1);
	}

	@TestFactory
	Stream<DynamicTest> cancelLike() {
		//given
		return Stream.of(
			dynamicTest("좋아요를 취소한다.", () -> {
				final PostLike postLike = post.likeFrom(otherUser);
				post.cancelLike(postLike);
				assertThat(post.getLikeCount()).isZero();
			}),
			dynamicTest("다른 방명록의 좋아요는 취소할 수 없다.", () -> {
				final Post otherPost = createPost(spot, otherUser);
				final PostLike otherPostLike = createPostLike(otherPost, otherUser);
				assertThatThrownBy(() -> post.cancelLike(otherPostLike))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(PostErrorCode.CAN_NOT_CANCEL_LIKE);
			}),
			dynamicTest("좋아요 개수는 0보다 줄어들 수 없다.", () -> {
				final Post otherPost = createPost(spot, otherUser);
				final PostLike unsavedPostLike = createPostLike(otherPost, otherUser);
				assertThatThrownBy(() -> post.cancelLike(unsavedPostLike))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(PostErrorCode.CAN_NOT_CANCEL_LIKE);
			}));
	}

	@TestFactory
	Stream<DynamicTest> updateDetailAddress() {
		// given
		assertEquals("디테일 주소", post.getDetailAddress());
		var newAddress = "새로운 디테일 주소";
		return Stream.of(
			dynamicTest("디테일 주소를 수정한다.", () -> {
				// when
				post.updateDetailAddress(writer, newAddress);

				// then
				assertEquals(newAddress, post.getDetailAddress());
			}),
			dynamicTest("다른 사람의 방명록을 수정하면 예외를 던진다.", () -> {

				// when & then
				assertThatThrownBy(() -> post.updateDetailAddress(otherUser, newAddress))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.PERMISSION_DENIED.getMessage());
			})
		);
	}

	@TestFactory
	Stream<DynamicTest> updatePrivacyState() {
		// given
		assertFalse(post.isPrivate());
		var isPrivate = true;

		return Stream.of(
			dynamicTest("방명록 공개 범위 상태를 변경한다.", () -> {
				// when
				post.updatePrivacyState(writer, isPrivate);

				// then
				assertTrue(post.isPrivate());
			}),
			dynamicTest("다른 사람의 방명록 공개 범위를 변경하면 예외를 던진다.", () -> {
				// when & then
				assertThatThrownBy(() -> post.updatePrivacyState(otherUser, isPrivate))
					.isInstanceOf(ApiException.class).hasMessage(AuthErrorCode.PERMISSION_DENIED.getMessage());
			})
		);
	}

	@Test
	void reportFromTest() {
		// when
		Long beforeLikeCount = post.getReportCount();
		Report report = post.reportFrom(otherUser, "신고 이유");

		// then
		assertAll(
			() -> assertThat(report.getPost()).isEqualTo(post),
			() -> assertThat(post.getReportCount()).isEqualTo(beforeLikeCount + 1)
		);
	}
}
