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

	@TestFactory
	Stream<DynamicTest> cancelLike() {
		//given
		return Stream.of(
			dynamicTest("좋아요를 취소한다.", () -> {
				assertThat(post.getLikeCount()).isZero();
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
		Report report = post.reportFrom(otherUser, "신고 이유");

		// then
		assertThat(report.getPost()).isEqualTo(post);
	}
}
