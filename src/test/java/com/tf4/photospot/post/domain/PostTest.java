package com.tf4.photospot.post.domain;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	void addPostTags() {
		List<PostTag> postTags = createPostTags(spot, post, tags);
		post.addPostTags(postTags);
		assertThat(post.getPostTags()).containsExactlyInAnyOrderElementsOf(postTags);
	}

	@Test
	void addMentions() {
		List<Mention> mentions = createMentions(post, users);
		post.addMentions(mentions);
		assertThat(post.getMentions()).containsExactlyInAnyOrderElementsOf(mentions);
	}
}
