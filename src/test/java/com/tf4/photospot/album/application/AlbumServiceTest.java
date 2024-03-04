package com.tf4.photospot.album.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.album.application.response.AlbumPreviewResponse;
import com.tf4.photospot.album.application.response.CreateAlbumPostResponse;
import com.tf4.photospot.album.domain.Album;
import com.tf4.photospot.album.domain.AlbumPost;
import com.tf4.photospot.album.domain.AlbumPostRepository;
import com.tf4.photospot.album.domain.AlbumRepository;
import com.tf4.photospot.album.domain.AlbumUser;
import com.tf4.photospot.album.domain.AlbumUserRepository;
import com.tf4.photospot.album.infrastructure.AlbumQueryRepository;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class AlbumServiceTest extends IntegrationTestSupport {
	private final AlbumService albumService;
	private final SpotRepository spotRepository;
	private final PostRepository postRepository;
	private final AlbumQueryRepository albumQueryRepository;
	private final UserRepository userRepository;
	private final AlbumRepository albumRepository;
	private final AlbumUserRepository albumUserRepository;
	private final AlbumPostRepository albumPostRepository;
	private final EntityManager em;

	@DisplayName("앨범 유저를 검증한다.")
	@Test
	void validateAlbumUser() {
		//given
		final User user = userRepository.save(createUser("user"));
		final Album album = albumRepository.save(createAlbum());
		final Album otherAlbum = albumRepository.save(createAlbum());
		albumUserRepository.save(new AlbumUser(user, album));
		//when then
		assertThat(albumQueryRepository.exixtsUserAlbum(user.getId(), album.getId())).isTrue();
		assertThat(albumQueryRepository.exixtsUserAlbum(user.getId(), otherAlbum.getId())).isFalse();
	}

	@DisplayName("앨범 방명록 미리보기 목록 조회")
	@TestFactory
	Stream<DynamicTest> getPostsOfAlbum() {
		//given
		Spot spot = spotRepository.save(createSpot());
		User user = userRepository.save(createUser("user"));
		Album album = albumRepository.save(new Album("album"));
		albumUserRepository.save(new AlbumUser(user, album));
		final List<Post> posts = postRepository.saveAll(createList(() -> createPost(spot, user), 5));
		final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
			.albumId(album.getId())
			.userId(user.getId())
			.type(PostSearchType.ALBUM_POSTS)
			.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
			.build();

		return Stream.of(
			dynamicTest("앨범에 속한 유저의 비공개 방명록 조회", () -> {
				final User otherUser = userRepository.save(createUser("otherUSer"));
				final Post otherUserPost = postRepository.save(createPost(spot, otherUser, true));
				albumPostRepository.save(new AlbumPost(otherUserPost, album));
				albumUserRepository.save(new AlbumUser(otherUser, album));
				var response = albumService.getPostPreviewsOfAlbum(postSearchCondition);
				assertThat(response.content().size()).isOne();
			}),
			dynamicTest("앨범에 다른 유저의 비공개 방명록은 조회 할 수 없다.", () -> {
				final User otherUser = userRepository.save(createUser("otherUser"));
				final Post otherUserPost = postRepository.save(createPost(spot, otherUser, true));
				albumPostRepository.save(new AlbumPost(otherUserPost, album));
				var response = albumService.getPostPreviewsOfAlbum(postSearchCondition);
				assertThat(response.content().size()).isOne();
			}),
			dynamicTest("앨범 방명록 조회", () -> {
				final List<AlbumPost> albumposts = posts.stream()
					.map(post -> new AlbumPost(post, album))
					.toList();
				albumPostRepository.saveAll(albumposts);
				var response = albumService.getPostPreviewsOfAlbum(postSearchCondition);
				assertThat(response.content().size()).isEqualTo(6);
			})
		);
	}

	@DisplayName("앨범 방명록 상세 목록 조회")
	@TestFactory
	Stream<DynamicTest> getPostPreviewsOfAlbum() {
		//given
		Spot spot = spotRepository.save(createSpot());
		User user = userRepository.save(createUser("user"));
		Album album = albumRepository.save(new Album("album"));
		albumUserRepository.save(new AlbumUser(user, album));
		final List<Post> posts = postRepository.saveAll(createList(() -> createPost(spot, user), 5));
		final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
			.albumId(album.getId())
			.userId(user.getId())
			.type(PostSearchType.ALBUM_POSTS)
			.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
			.build();

		return Stream.of(
			dynamicTest("앨범에 속한 유저의 비공개 방명록 상세 조회", () -> {
				final User otherUser = userRepository.save(createUser("otherUSer"));
				final Post otherUserPost = postRepository.save(createPost(spot, otherUser, true));
				albumPostRepository.save(new AlbumPost(otherUserPost, album));
				albumUserRepository.save(new AlbumUser(otherUser, album));
				var response = albumService.getPostsOfAlbum(postSearchCondition);
				assertThat(response.content().size()).isOne();
			}),
			dynamicTest("앨범에 다른 유저의 비공개 방명록은 상세 조회 할 수 없다.", () -> {
				final User otherUser = userRepository.save(createUser("otherUser"));
				final Post otherUserPost = postRepository.save(createPost(spot, otherUser, true));
				albumPostRepository.save(new AlbumPost(otherUserPost, album));
				var response = albumService.getPostsOfAlbum(postSearchCondition);
				assertThat(response.content().size()).isOne();
			}),
			dynamicTest("앨범 방명록 상세 조회", () -> {
				final List<AlbumPost> albumposts = posts.stream()
					.map(post -> new AlbumPost(post, album))
					.toList();
				albumPostRepository.saveAll(albumposts);
				var response = albumService.getPostsOfAlbum(postSearchCondition);
				assertThat(response.content().size()).isEqualTo(6);
			})
		);
	}

	@DisplayName("앨범 테스트")
	@TestFactory
	Stream<DynamicTest> createAlbumTest() {
		//given
		final Spot spot = spotRepository.save(createSpot());
		final User user = userRepository.save(createUser("user"));
		final Long albumId = albumService.create(user.getId(), "album");
		final List<Post> posts = postRepository.saveAll(createList(() -> createPost(spot, user), 5));
		final List<Post> newPosts = postRepository.saveAll(createList(() -> createPost(spot, user), 5));

		return Stream.of(
			dynamicTest("앨범을 생성한다.", () -> {
				assertThat(albumRepository.findById(albumId))
					.isPresent().get()
					.extracting("name")
					.isEqualTo("album");
				assertThat(albumQueryRepository.exixtsUserAlbum(user.getId(), albumId)).isTrue();
			}),
			dynamicTest("앨범에 방명록을 추가한다.", () -> {
				final List<Long> postIds = posts.stream().map(Post::getId).toList();
				final List<CreateAlbumPostResponse> responses = albumService.addPosts(postIds, albumId,
					user.getId());
				assertThat(responses).extracting("postId").isEqualTo(postIds);
			}),
			dynamicTest("앨범에 중복된 방명록을 추가할 수 없다.", () -> {
				final Post existPost = posts.get(0);
				final List<Long> postIds = newPosts.stream().map(Post::getId).collect(Collectors.toList());
				postIds.add(existPost.getId());
				final List<CreateAlbumPostResponse> responses = albumService.addPosts(postIds, albumId,
					user.getId());
				final Optional<CreateAlbumPostResponse> result = responses.stream()
					.filter(response -> response.postId().equals(existPost.getId()))
					.findFirst();
				assertThat(result).isEmpty();
				assertThat(albumPostRepository.findAll().size()).isEqualTo(posts.size() + newPosts.size());
			}),
			dynamicTest("앨범 방명록을 제거한다.", () -> {
				final List<Long> postIds = newPosts.stream().map(Post::getId).collect(Collectors.toList());
				assertThatNoException().isThrownBy(() -> albumService.removePosts(postIds, albumId, user.getId()));
				assertThat(albumPostRepository.findAll().size()).isEqualTo(posts.size());
			}),
			dynamicTest("앨범에 없는 방명록을 제거하면 CONTAINS_ALBUM_POSTS_CANNOT_DELETE 예외가 발생한다.", () -> {
				final List<Long> noExistPostIds = newPosts.stream().map(Post::getId).collect(Collectors.toList());
				assertThatThrownBy(() -> albumService.removePosts(noExistPostIds, albumId, user.getId()))
					.hasMessage("삭제할 수 없는 앨범 방명록이 포함되어 있습니다.");
			}),
			dynamicTest("앨범을 삭제한다.", () -> {
				albumService.remove(albumId, user.getId());
				em.clear();
				assertThat(albumRepository.findById(albumId)).isEmpty();
				assertThat(albumPostRepository.findAll()).isEmpty();
				assertThat(albumQueryRepository.exixtsUserAlbum(user.getId(), albumId)).isFalse();
			})
		);
	}

	@DisplayName("앨범 리스트 조회")
	@TestFactory
	Stream<DynamicTest> albumListTest() {
		//given
		final Spot spot = spotRepository.save(createSpot());
		final User user1 = userRepository.save(createUser("user1"));
		final User user2 = userRepository.save(createUser("user2"));
		final Album album1 = albumRepository.save(new Album("album1"));
		final Album album2 = albumRepository.save(new Album("album2"));
		final Album emptyAlbum = albumRepository.save(new Album("album3"));
		albumUserRepository.save(new AlbumUser(user1, album1));
		albumUserRepository.save(new AlbumUser(user1, album2));
		albumUserRepository.save(new AlbumUser(user2, album1));
		albumUserRepository.save(new AlbumUser(user2, album2));
		Post post1 = postRepository.save(createPost(spot, user1, createPhoto("photoUrl1")));
		Post post2 = postRepository.save(createPost(spot, user1, createPhoto("photoUrl2")));
		albumService.addPosts(List.of(post1.getId()), album1.getId(), user1.getId());
		albumService.addPosts(List.of(post2.getId()), album2.getId(), user1.getId());

		return Stream.of(
			dynamicTest("앨범은 먼저 생성된 순서대로 조회한다.", () -> {
				//when
				final List<AlbumPreviewResponse> albumResponses = albumService.getAlbums(user1.getId());
				//then
				assertThat(albumResponses).hasSize(2);
				assertThat(albumResponses).extracting("albumId")
					.containsExactly(album1.getId(), album2.getId());
			}),
			dynamicTest("가장 최근에 추가된 앨범 방명록을 미리보기로 조회한다.", () -> {
				//when
				Post latestPost = postRepository.save(createPost(spot, user1, createPhoto("photoUrl3")));
				albumService.addPosts(List.of(latestPost.getId()), album1.getId(), user1.getId());
				final List<AlbumPreviewResponse> albumResponses = albumService.getAlbums(user1.getId());
				//then
				assertThat(albumResponses).hasSize(2);
				assertThat(albumResponses).extracting("photoUrl").containsAnyOf("photoUrl3");
			}),
			dynamicTest("가장 최근에 추가된 방명록이 비공개 되는 경우 앨범 유저의 방명록이 아니면 조회할 수 없다.", () -> {
				//given
				final User user3 = userRepository.save(createUser("user3"));
				Post privatePost = postRepository.save(createPost(spot, user3,
					createPhoto("photoUrl4"), 0L, true));
				albumService.addPosts(List.of(privatePost.getId()), album1.getId(), user1.getId());
				//when
				final List<AlbumPreviewResponse> albumResponses = albumService.getAlbums(user1.getId());
				//then
				assertThat(albumResponses).extracting("photoUrl")
					.isNotEmpty()
					.doesNotContain("photoUrl4");
			}),
			dynamicTest("가장 최근에 추가된 방명록이 비공개 되는 경우 앨범 유저의 방명록이면 조회할 수 있다.", () -> {
				//given
				Post privatePost = postRepository.save(createPost(spot, user1,
					createPhoto("photoUrl5"), 0L, true));
				albumService.addPosts(List.of(privatePost.getId()), album1.getId(), user1.getId());
				//when
				final List<AlbumPreviewResponse> albumResponses = albumService.getAlbums(user1.getId());
				//then
				assertThat(albumResponses).extracting("photoUrl").containsAnyOf("photoUrl5");
			}),
			dynamicTest("비어있는 앨범 조회시 imageUrl이 \"\" 로 조회된다.", () -> {
				//given
				final AlbumPreviewResponse emptyAlbumPreviewResponse = AlbumPreviewResponse.builder()
					.albumId(emptyAlbum.getId())
					.name(emptyAlbum.getName())
					.photoUrl("")
					.build();
				albumUserRepository.save(new AlbumUser(user1, emptyAlbum));
				//when
				final List<AlbumPreviewResponse> albumResponses = albumService.getAlbums(user1.getId());
				//then
				final Optional<AlbumPreviewResponse> emptyAlbumResult = albumResponses.stream()
					.filter(albumResponse -> albumResponse.albumId().equals(emptyAlbum.getId()))
					.findFirst();
				assertThat(emptyAlbumResult).isPresent().get().isEqualTo(emptyAlbumPreviewResponse);
			}),
			dynamicTest("가장 최근에 추가된 앨범 방명록이 삭제 되는 경우 조회 되지 않는다.", () -> {
				Post latestPost = postRepository.save(createPost(spot, user1, createPhoto("photoUrl6")));
				albumService.addPosts(List.of(latestPost.getId()), album1.getId(), user1.getId());
				latestPost.delete(user1);
				postRepository.save(latestPost);
				em.flush();
				//when
				final List<AlbumPreviewResponse> albumResponses = albumService.getAlbums(user1.getId());
				//then
				assertThat(albumResponses).extracting("photoUrl")
					.isNotEmpty()
					.doesNotContain("photoUrl6");
			})
		);
	}
}
