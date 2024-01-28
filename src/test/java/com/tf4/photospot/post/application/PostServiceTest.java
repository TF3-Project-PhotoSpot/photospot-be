package com.tf4.photospot.post.application;

import static com.tf4.photospot.support.TestFixture.*;
import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLikeRepository;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.post.domain.PostTagRepository;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.post.domain.TagRepository;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PostServiceTest extends IntegrationTestSupport {
	private final PostService postService;
	private final SpotRepository spotRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;
	private final PostTagRepository postTagRepository;
	private final TagRepository tagRepository;

	@DisplayName("방명록 상세 목록 조회")
	@TestFactory
	Stream<DynamicTest> getPosts() {
		//given
		Spot spot = createSpot();
		User writer = createUser("작성자");
		User reader = createUser("읽는이");
		spotRepository.save(spot);
		userRepository.saveAll(List.of(writer, reader));
		// Dummy posts
		List<Post> posts = createList(() -> createPost(spot, writer), 15);
		postRepository.saveAll(posts);
		// 마지막에 추가된 포스트는 태그, 좋아요 정보를 가지고 있다.
		Post lastPost = createPost(spot, writer, createPhoto("firstPhotoUrl"), createPoint());
		postRepository.save(lastPost);
		List<Tag> tags = tagRepository.saveAll(createTags("tagA", "tagB", "tagC"));
		postTagRepository.saveAll(createPostTags(spot, lastPost, tags));
		postLikeRepository.save(createPostLike(lastPost, reader));
		// Common Request
		PostListRequest firstPageRequest = new PostListRequest(spot.getId(), reader.getId(),
			PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id"))));

		return Stream.of(
			dynamicTest("방명록 상세 목록을 슬라이스 페이징으로 조회한다.", () -> {
				//given
				PostListRequest lastPageRequest = new PostListRequest(spot.getId(), reader.getId(),
					PageRequest.of(1, 10, Sort.by(Sort.Order.desc("id"))));
				//when
				SlicePageDto<PostDetailResponse> firstResponse = postService.getPosts(firstPageRequest);
				SlicePageDto<PostDetailResponse> lastResponse = postService.getPosts(lastPageRequest);
				//then
				assertThat(firstResponse.hasNext()).isTrue();
				assertThat(firstResponse.content().size()).isEqualTo(firstPageRequest.pageable().getPageSize());
				assertThat(lastResponse.hasNext()).isFalse();
				assertThat(lastResponse.content().size()).isLessThan(lastPageRequest.pageable().getPageSize());
			}),
			dynamicTest("태그, 좋아요 정보를 조회할 수 있다.", () -> {
				//when
				SlicePageDto<PostDetailResponse> response = postService.getPosts(firstPageRequest);
				//then
				PostDetailResponse postWithTagAndLikeStatus = response.content().get(0);
				PostDetailResponse postWithoutTagAndLikeStatus = response.content().get(1);
				assertThat(postWithTagAndLikeStatus.writer().id()).isEqualTo(writer.getId());
				assertThat(postWithTagAndLikeStatus.tags()).extracting("tagName")
					.containsExactly("tagA", "tagB", "tagC");
				assertThat(postWithTagAndLikeStatus.isLiked()).isTrue();
				assertThat(postWithTagAndLikeStatus.photoUrl()).isNotBlank();
				assertThat(postWithoutTagAndLikeStatus.tags()).isEmpty();
				assertThat(postWithoutTagAndLikeStatus.isLiked()).isFalse();
			}),
			dynamicTest("최신순으로 조회할 수 있다.", () -> {
				//given when
				SlicePageDto<PostDetailResponse> postsSortedByLatest = postService.getPosts(
					new PostListRequest(spot.getId(), reader.getId(),
						PageRequest.of(0, 10, Sort.by(Sort.Order.desc("id")))));
				SlicePageDto<PostDetailResponse> postsSortedByOldest = postService.getPosts(
					new PostListRequest(spot.getId(), reader.getId(),
						PageRequest.of(0, 10, Sort.by(Sort.Order.asc("id")))));
				//then
				assertThatList(postsSortedByLatest.content()).isSortedAccordingTo(
					comparing(PostDetailResponse::id).reversed());
				assertThatList(postsSortedByOldest.content()).isSortedAccordingTo(
					comparing(PostDetailResponse::id));
			}),
			dynamicTest("좋아요순으로 조회할 수 있다.", () -> {
				//given when
				SlicePageDto<PostDetailResponse> postsSortedByMostLikeCount = postService.getPosts(
					new PostListRequest(spot.getId(), reader.getId(),
						PageRequest.of(0, 10, Sort.by(Sort.Order.desc("likeCount")))));
				SlicePageDto<PostDetailResponse> postsSortedByLeastLikeCount = postService.getPosts(
					new PostListRequest(spot.getId(), reader.getId(),
						PageRequest.of(0, 10, Sort.by(Sort.Order.asc("likeCount")))));
				//then
				assertThatList(postsSortedByMostLikeCount.content()).isSortedAccordingTo(
					comparing(PostDetailResponse::likeCount).reversed());
				assertThatList(postsSortedByLeastLikeCount.content()).isSortedAccordingTo(
					comparing(PostDetailResponse::likeCount));
			}),
			dynamicTest("좋아요순으로, 좋아요가 같으면 최신순으로 조회할 수 있다.", () -> {
				//given when
				final Long mostLikeCount = 1000L;
				postRepository.saveAll(createList(() -> createPost(spot, writer, mostLikeCount), 5));
				SlicePageDto<PostDetailResponse> postsSortedByMostLikeCountAndLatest = postService.getPosts(
					new PostListRequest(spot.getId(), reader.getId(),
						PageRequest.of(0, 10, Sort.by(
							Sort.Order.desc("likeCount"),
							Sort.Order.desc("id")))));
				//then
				assertThatList(postsSortedByMostLikeCountAndLatest.content())
					.isSortedAccordingTo(comparing(PostDetailResponse::likeCount).reversed()
						.thenComparing(comparing(PostDetailResponse::id).reversed()));
			})
		);
	}
}
