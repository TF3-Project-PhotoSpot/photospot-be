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
				assertThat(responses).allMatch(response -> !response.isDuplicated());
			}),
			dynamicTest("앨범에 중복된 방명록을 추가할 수 없다.", () -> {
				final Post existPost = posts.get(0);
				final List<Long> postIds = newPosts.stream().map(Post::getId).collect(Collectors.toList());
				postIds.add(existPost.getId());
				final List<CreateAlbumPostResponse> responses = albumService.addPosts(postIds, albumId,
					user.getId());
				final Optional<CreateAlbumPostResponse> createFailAlbumPost = responses.stream()
					.filter(response -> response.postId().equals(existPost.getId()))
					.findFirst();
				assertThat(createFailAlbumPost).isPresent().get().matches(CreateAlbumPostResponse::isDuplicated);
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
}
