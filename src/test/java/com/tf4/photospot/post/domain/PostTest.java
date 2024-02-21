package com.tf4.photospot.post.domain;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

public class PostTest {
	private Spot spot;
	private Post post;
	private List<Tag> tags;
	private List<User> users;

	@BeforeEach
	void setUp() {
		User writer = createUser("작성자");
		Photo photo = createPhoto();
		spot = createSpot();
		post = createPost(spot, writer, photo);
		tags = createTags("tagA", "tagB", "tagC");
		users = List.of(createUser("사용자1"), createUser("사용자2"));
	}

	@Test
	void delete() {
		post.delete();
		assertNotNull(post.getDeletedAt());
	}

	@Test
	void likeFromTest() {
		//when
		final Long beforeLikeCount = post.getLikeCount();
		final PostLike postLike = post.likeFrom(users.get(0));
		//then
		assertThat(postLike.getPost()).isEqualTo(post);
		assertThat(post.getLikeCount()).isEqualTo(beforeLikeCount + 1);
	}

	@TestFactory
	Stream<DynamicTest> cancelLike() {
		//given
		return Stream.of(
			dynamicTest("좋아요를 취소한다.", () -> {
				final PostLike postLike = post.likeFrom(users.get(0));
				post.cancelLike(postLike);
				assertThat(post.getLikeCount()).isZero();
			}),
			dynamicTest("다른 방명록의 좋아요는 취소할 수 없다.", () -> {
				final Post otherPost = createPost(spot, users.get(0));
				final PostLike otherPostLike = createPostLike(otherPost, users.get(0));
				assertThatThrownBy(() -> post.cancelLike(otherPostLike))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(PostErrorCode.CAN_NOT_CANCEL_LIKE);
			}),
			dynamicTest("좋아요 개수는 0보다 줄어들 수 없다.", () -> {
				final Post otherPost = createPost(spot, users.get(0));
				final PostLike unsavedPostLike = createPostLike(otherPost, users.get(0));
				assertThatThrownBy(() -> post.cancelLike(unsavedPostLike))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(PostErrorCode.CAN_NOT_CANCEL_LIKE);
			}));
	}
}
