package com.tf4.photospot.post.application;

import static com.tf4.photospot.support.TestFixture.*;
import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.application.request.PostUpdateRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.ReportResponse;
import com.tf4.photospot.post.application.response.TagResponse;
import com.tf4.photospot.post.domain.Mention;
import com.tf4.photospot.post.domain.MentionRepository;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostLikeRepository;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.post.domain.PostTag;
import com.tf4.photospot.post.domain.PostTagRepository;
import com.tf4.photospot.post.domain.Report;
import com.tf4.photospot.post.domain.ReportRepository;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.post.domain.TagRepository;
import com.tf4.photospot.post.presentation.request.BubbleInfoDto;
import com.tf4.photospot.post.presentation.request.PhotoInfoDto;
import com.tf4.photospot.post.presentation.request.PostUpdateHttpRequest;
import com.tf4.photospot.post.presentation.request.PostUploadRequest;
import com.tf4.photospot.post.presentation.request.SpotInfoDto;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.support.IntegrationTestSupport;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Import(MockS3Config.class)
class PostServiceTest extends IntegrationTestSupport {
	private final PostService postService;
	private final SpotRepository spotRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;
	private final PostTagRepository postTagRepository;
	private final TagRepository tagRepository;
	private final MentionRepository mentionRepository;
	private final ReportRepository reportRepository;

	@DisplayName("방명록 좋아요")
	@TestFactory
	Stream<DynamicTest> likePost() {
		//given
		final Spot spot = spotRepository.save(createSpot());
		final User writer = userRepository.save(createUser("이성빈"));
		final Post post = postRepository.save(createPost(spot, writer, 0L));
		final User user = userRepository.save(createUser("user"));

		return Stream.of(
			dynamicTest("좋아요를 할 수 있다.", () -> {
				//given
				final Long beforeLikes = post.getLikeCount();
				//when
				postService.likePost(post.getId(), user.getId());
				//then
				assertThat(postRepository.findById(post.getId())).isPresent()
					.get()
					.satisfies(updatedPost -> assertThat(updatedPost.getLikeCount()).isEqualTo(beforeLikes + 1));
			}), dynamicTest("좋아요를 중복해서 할 수 없다.",
				() -> assertThatThrownBy(() -> postService.likePost(post.getId(), user.getId())).isInstanceOf(
					ApiException.class).extracting("errorCode").isEqualTo(PostErrorCode.ALREADY_LIKE)),
			dynamicTest("좋아요 취소를 할 수 있다.", () -> {
				//given
				final Long beforeLikes = post.getLikeCount();
				//when
				postService.cancelPostLike(post.getId(), user.getId());
				//then
				assertThat(postRepository.findById(post.getId())).isPresent()
					.get()
					.satisfies(updatedPost -> assertThat(updatedPost.getLikeCount()).isEqualTo(beforeLikes - 1));
			}), dynamicTest("좋아요 취소를 중복해서 할 수 없다.",
				() -> assertThatThrownBy(() -> postService.cancelPostLike(post.getId(), user.getId())).isInstanceOf(
					ApiException.class).extracting("errorCode").isEqualTo(PostErrorCode.NO_EXISTS_LIKE)));
	}

	@DisplayName("방명록 미리보기 목록 조회")
	@TestFactory
	Stream<DynamicTest> getPostPreviews() {
		//given
		Spot spot = spotRepository.save(createSpot());
		User writer = userRepository.save(createUser("작성자"));
		User reader = userRepository.save(createUser("읽는이"));
		// Dummy posts
		List<Post> posts = postRepository.saveAll(createList(() -> createPost(spot, writer), 15));
		// Common Request
		var firstPageRequest = PostSearchCondition.builder()
			.spotId(spot.getId())
			.userId(writer.getId())
			.type(PostSearchType.POSTS_OF_SPOT)
			.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
			.build();

		return Stream.of(dynamicTest("슬라이스 페이징으로 조회한다.", () -> {
			//given
			var lastPageRequest = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			SlicePageDto<PostPreviewResponse> firstResponse = postService.getPostPreviews(firstPageRequest);
			SlicePageDto<PostPreviewResponse> lastResponse = postService.getPostPreviews(lastPageRequest);
			//then
			assertThat(firstResponse.hasNext()).isTrue();
			assertThat(firstResponse.content().size()).isEqualTo(firstPageRequest.pageable().getPageSize());
			assertThat(lastResponse.hasNext()).isFalse();
			assertThat(lastResponse.content().size()).isLessThan(lastPageRequest.pageable().getPageSize());
		}), dynamicTest("좋아요순으로 조회할 수 있다.", () -> {
			//given
			var allPostRequest = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "likeCount")))
				.build();
			//when
			SlicePageDto<PostPreviewResponse> response = postService.getPostPreviews(allPostRequest);
			final List<Long> postIdsSortedLikeCountDesc = posts.stream()
				.sorted(comparing(Post::getLikeCount).reversed())
				.map(Post::getId)
				.toList();
			//then
			assertThatList(response.content().stream().map(PostPreviewResponse::postId).toList()).isEqualTo(
				postIdsSortedLikeCountDesc);
		}), dynamicTest("삭제 또는 신고 되었거나 비공개 방명록은 조회할 수 없다.", () -> {
			//given
			Post privatePost = createPost(spot, writer, true);
			Post deletePost = createPost(spot, writer);
			Post reportedPost = createPost(spot, writer);
			deletePost.delete(writer);
			postRepository.saveAll(List.of(privatePost, deletePost, reportedPost));
			reportRepository.save(reportedPost.reportFrom(reader, "불쾌한 사진"));

			var latestPostRequest = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			SlicePageDto<PostPreviewResponse> response = postService.getPostPreviews(latestPostRequest);
			//then
			assertThat(response.content().get(0).postId()).isNotIn(privatePost.getId(), deletePost.getId(),
				reportedPost.getId());
		}), dynamicTest("내 방명록만 조회 시 다른 유저가 작성한 방명록은 볼 수 없다.", () -> {
			//given
			final Post otherUserPost = postRepository.save(createPost(spot, reader, false));
			final PostSearchCondition searchCondition = PostSearchCondition.builder()
				.userId(writer.getId())
				.type(PostSearchType.MY_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			final boolean visibleOtherWriterPost = postService.getPostPreviews(searchCondition)
				.content()
				.stream()
				.anyMatch(postPreview -> postPreview.postId().equals(otherUserPost.getId()));
			//then
			assertFalse(visibleOtherWriterPost);
		}), dynamicTest("내 방명록 조회중 삭제된 것은 볼 수 없고 비공개는 볼 수 있다.", () -> {
			//given
			final Post privatePost = postRepository.save(createPost(spot, writer, true));
			final Post deletePost = createPost(spot, writer, false);
			deletePost.delete(writer);
			postRepository.save(deletePost);
			final PostSearchCondition searchCondition = PostSearchCondition.builder()
				.userId(writer.getId())
				.type(PostSearchType.MY_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			List<PostPreviewResponse> response = postService.getPostPreviews(searchCondition).content();
			final boolean visiblePrivatePost = response.stream()
				.anyMatch(postPreview -> postPreview.postId().equals(privatePost.getId()));
			final boolean visibleDeletedPost = response.stream()
				.anyMatch(postPreview -> postPreview.postId().equals(deletePost.getId()));
			//then
			assertTrue(visiblePrivatePost);
			assertFalse(visibleDeletedPost);
		}), dynamicTest("내가 좋아요한 방명록이 없는 경우 조회 결과가 나오지 않는다.", () -> {
			final User user = userRepository.save(createUser("user"));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPostPreviews(postSearchCondition);
			assertThat(response.content()).isEmpty();
			assertThat(response.hasNext()).isFalse();
		}), dynamicTest("내가 좋아요한 방명록 미리보기 목록을 조회할 수 있다.", () -> {
			final User user = userRepository.save(createUser("user"));
			int postLikeCount = 5;
			posts.subList(0, postLikeCount).forEach(post -> postLikeRepository.save(createPostLike(post, user)));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, postLikeCount, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPostPreviews(postSearchCondition);
			assertThat(response.content().size()).isEqualTo(postLikeCount);
			assertThat(response.hasNext()).isFalse();
		}), dynamicTest("내가 좋아요한 방명록 목록을 좋아요한 순서로 조회 수 있다.", () -> {
			final User user = userRepository.save(createUser("user"));
			final PostLike like1 = postLikeRepository.save(createPostLike(posts.get(0), user));
			final PostLike like2 = postLikeRepository.save(createPostLike(posts.get(1), user));
			final PostLike like3 = postLikeRepository.save(createPostLike(posts.get(2), user));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPostPreviews(postSearchCondition);
			assertThat(response.content()).extracting(PostPreviewResponse::postId)
				.containsExactly(like3.getPost().getId(), like2.getPost().getId(), like1.getPost().getId());
		}), dynamicTest("내가 좋아요한 방명록 중 신고한 방명록은 조회 결과에서 제외된다.", () -> {
			final User user = userRepository.save(createUser("user"));
			int postLikeCount = 5;
			List<Post> likedPosts = posts.subList(0, postLikeCount);
			likedPosts.forEach(post -> postLikeRepository.save(createPostLike(post, user)));
			reportRepository.save(likedPosts.get(0).reportFrom(user, "불쾌한 사진"));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPostPreviews(postSearchCondition);
			assertThat(response.content().size()).isEqualTo(postLikeCount - 1);
			assertThat(response.content().stream().map(PostPreviewResponse::postId).toList()).doesNotContain(
				likedPosts.get(0).getId());
			assertFalse(response.hasNext());
		}));
	}

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
		// 마지막에 추가된 포스트는 사진 버블, 태그, 좋아요 정보를 가지고 있다.
		Photo photoWithBubble = createPhotoWithBubble("firstPhotoUrl", "이미지 버블", 100, 200);
		Post lastPost = createPost(spot, writer, photoWithBubble);
		postRepository.save(lastPost);
		List<Tag> tags = tagRepository.saveAll(createTags("tagA", "tagB", "tagC"));
		postTagRepository.saveAll(createPostTags(spot, lastPost, tags));
		postLikeRepository.save(createPostLike(lastPost, reader));
		// Common Request
		final PostSearchCondition firstPageRequest = PostSearchCondition.builder()
			.spotId(spot.getId())
			.userId(reader.getId())
			.type(PostSearchType.POSTS_OF_SPOT)
			.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
			.build();

		return Stream.of(dynamicTest("방명록 상세 목록을 슬라이스 페이징으로 조회한다.", () -> {
			//given
			PostSearchCondition lastPageRequest = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(writer.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			SlicePageDto<PostDetailResponse> firstResponse = postService.getPosts(firstPageRequest);
			SlicePageDto<PostDetailResponse> lastResponse = postService.getPosts(lastPageRequest);
			//then
			assertThat(firstResponse.hasNext()).isTrue();
			assertThat(firstResponse.content().size()).isEqualTo(firstPageRequest.pageable().getPageSize());
			assertThat(lastResponse.hasNext()).isFalse();
			assertThat(lastResponse.content().size()).isLessThan(lastPageRequest.pageable().getPageSize());
		}), dynamicTest("버블, 태그, 좋아요 정보를 조회할 수 있다.", () -> {
			//when
			SlicePageDto<PostDetailResponse> response = postService.getPosts(firstPageRequest);
			//then
			PostDetailResponse postWithExtraInfo = response.content().get(0);
			PostDetailResponse postWithoutAnyInfo = response.content().get(1);
			assertThat(postWithExtraInfo.writer().id()).isEqualTo(writer.getId());
			assertThat(postWithExtraInfo.tags()).extracting("tagName").containsExactly("tagA", "tagB", "tagC");
			assertThat(postWithExtraInfo.isLiked()).isTrue();
			assertThat(postWithExtraInfo.photoUrl()).isNotBlank();
			assertThat(postWithExtraInfo.bubble().text()).isEqualTo("이미지 버블");
			assertThat(postWithExtraInfo.bubble().x()).isEqualTo(100);
			assertThat(postWithExtraInfo.bubble().y()).isEqualTo(200);
			assertThat(postWithoutAnyInfo.tags()).isEmpty();
			assertThat(postWithoutAnyInfo.isLiked()).isFalse();
			assertThat(postWithoutAnyInfo.bubble()).isNull();
		}), dynamicTest("최신순으로 조회할 수 있다.", () -> {
			//given
			final PostSearchCondition searchByLatest = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			final PostSearchCondition searchByOldest = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")))
				.build();
			// when
			SlicePageDto<PostDetailResponse> postsSortedByLatest = postService.getPosts(searchByLatest);
			SlicePageDto<PostDetailResponse> postsSortedByOldest = postService.getPosts(searchByOldest);
			//then
			assertThatList(postsSortedByLatest.content()).isSortedAccordingTo(
				comparing(PostDetailResponse::id).reversed());
			assertThatList(postsSortedByOldest.content()).isSortedAccordingTo(comparing(PostDetailResponse::id));
		}), dynamicTest("좋아요순으로 조회할 수 있다.", () -> {
			//given
			final PostSearchCondition searchByMostLikes = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("likeCount"))))
				.build();
			final PostSearchCondition searchByLeastLikes = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Order.asc("likeCount"))))
				.build();
			// when
			SlicePageDto<PostDetailResponse> postsSortedByMostLikeCount = postService.getPosts(searchByMostLikes);
			SlicePageDto<PostDetailResponse> postsSortedByLeastLikeCount = postService.getPosts(searchByLeastLikes);
			//then
			assertThatList(postsSortedByMostLikeCount.content()).isSortedAccordingTo(
				comparing(PostDetailResponse::likeCount).reversed());
			assertThatList(postsSortedByLeastLikeCount.content()).isSortedAccordingTo(
				comparing(PostDetailResponse::likeCount));
		}), dynamicTest("좋아요순으로, 좋아요가 같으면 최신순으로 조회할 수 있다.", () -> {
			//given
			final Long mostLikeCount = 1000L;
			final PostSearchCondition searchCondition = PostSearchCondition.builder()
				.spotId(spot.getId())
				.userId(reader.getId())
				.type(PostSearchType.POSTS_OF_SPOT)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("id"))))
				.build();
			//when
			postRepository.saveAll(createList(() -> createPost(spot, writer, mostLikeCount), 5));
			SlicePageDto<PostDetailResponse> postsSortedByMostLikeCountAndLatest = postService.getPosts(
				searchCondition);
			//then
			assertThatList(postsSortedByMostLikeCountAndLatest.content()).isSortedAccordingTo(
				comparing(PostDetailResponse::likeCount).reversed()
					.thenComparing(comparing(PostDetailResponse::id).reversed()));
		}), dynamicTest("내 방명록 상세 조회 시 다른 유저가 작성한 방명록은 볼 수 없다.", () -> {
			//given
			final Post otherUserPost = postRepository.save(createPost(spot, reader, false));
			final PostSearchCondition searchCondition = PostSearchCondition.builder()
				.userId(writer.getId())
				.type(PostSearchType.MY_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			final boolean visibleOtherWriterPost = postService.getPosts(searchCondition)
				.content()
				.stream()
				.anyMatch(postDetailResponse -> postDetailResponse.id().equals(otherUserPost.getId()));
			//then
			assertFalse(visibleOtherWriterPost);
		}), dynamicTest("내 방명록 상세 조회시 삭제된 것은 볼 수 없고 비공개는 볼 수 있다.", () -> {
			//given
			final Post privatePost = postRepository.save(createPost(spot, writer, true));
			final Post deletePost = createPost(spot, writer, false);
			deletePost.delete(writer);
			postRepository.save(deletePost);
			final PostSearchCondition searchCondition = PostSearchCondition.builder()
				.userId(writer.getId())
				.type(PostSearchType.MY_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			//when
			List<PostDetailResponse> response = postService.getPosts(searchCondition).content();
			final boolean visiblePrivatePost = response.stream()
				.anyMatch(postDetailResponse -> postDetailResponse.id().equals(privatePost.getId()));
			final boolean visibleDeletedPost = response.stream()
				.anyMatch(postDetailResponse -> postDetailResponse.id().equals(deletePost.getId()));
			//then
			assertTrue(visiblePrivatePost);
			assertFalse(visibleDeletedPost);
		}), dynamicTest("내가 좋아요한 방명록이 없는 경우 상세 조회 결과가 나오지 않는다.", () -> {
			final User user = userRepository.save(createUser("user"));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPosts(postSearchCondition);
			assertThat(response.content()).isEmpty();
			assertThat(response.hasNext()).isFalse();
		}), dynamicTest("내가 좋아요한 방명록 상세 목록을 조회할 수 있다.", () -> {
			final User user = userRepository.save(createUser("user"));
			int postLikeCount = 5;
			posts.subList(0, postLikeCount).forEach(post -> postLikeRepository.save(createPostLike(post, user)));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, postLikeCount, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPosts(postSearchCondition);
			assertThat(response.content().size()).isEqualTo(postLikeCount);
			assertThat(response.hasNext()).isFalse();
		}), dynamicTest("내가 좋아요한 방명록 상세 목록을 좋아요한 순서로 조회 수 있다.", () -> {
			final User user = userRepository.save(createUser("user"));
			final PostLike like1 = postLikeRepository.save(createPostLike(posts.get(0), user));
			final PostLike like2 = postLikeRepository.save(createPostLike(posts.get(1), user));
			final PostLike like3 = postLikeRepository.save(createPostLike(posts.get(2), user));
			final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
				.userId(user.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			var response = postService.getPosts(postSearchCondition);
			assertThat(response.content()).extracting(PostDetailResponse::id)
				.containsExactly(like3.getPost().getId(), like2.getPost().getId(), like1.getPost().getId());
		}), dynamicTest("내가 좋아요한 방명록 중 삭제됐거나 신고한 방명록이 있으면 해당 방명록은 상세 목록에서 제외한다.", () -> {
			// given
			final PostLike likedPost = postLikeRepository.save(createPostLike(posts.get(0), reader));
			final PostLike likedAndDeletedPost = postLikeRepository.save(createPostLike(posts.get(1), reader));
			final PostLike likedAndReportedPost = postLikeRepository.save(createPostLike(posts.get(2), reader));
			likedAndDeletedPost.getPost().delete(writer);
			reportRepository.save(likedAndReportedPost.getPost().reportFrom(reader, "불쾌한 사진"));
			final PostSearchCondition searchCondition = PostSearchCondition.builder()
				.userId(reader.getId())
				.type(PostSearchType.LIKE_POSTS)
				.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
				.build();
			// when
			List<PostDetailResponse> response = postService.getPosts(searchCondition).content();
			// then
			assertThat(response.get(0).id()).isIn(likedPost.getPost().getId())
				.isNotIn(likedAndReportedPost.getPost().getId(), likedAndDeletedPost.getPost().getId());
		}));
	}

	@TestFactory
	@DisplayName("방명록 등록 시나리오")
	Collection<DynamicTest> uploadPost() {
		// given
		var photoInfo = new PhotoInfoDto("https://bucket.s3.ap-northeast-2.amazonaws.com/temp/example.webp",
			new CoordinateDto(35.512, 126.912), "2024-01-13T05:20:18.981+09:00");
		var bubbleInfo = new BubbleInfoDto("이미지 설명", 100, 200);
		var spotInfo = new SpotInfoDto(new CoordinateDto(35.557, 126.923), "중점 좌표 기준 변환된 주소");
		User writer = createUser("작성자");
		User mentionedUser = createUser("언급된 사용자");
		userRepository.saveAll(List.of(writer, mentionedUser));
		List<Tag> tags = tagRepository.saveAll(createTags("tagA", "tagB", "tagC"));
		var tagIds = List.of(tags.get(0).getId(), tags.get(1).getId(), tags.get(2).getId());
		var mentionedUserIds = List.of(mentionedUser.getId());
		return List.of(
			dynamicTest("스팟이 존재하지 않으면 스팟 생성 후 방명록을 업로드하고 그와 관련된 엔티티를 저장한다.", () -> {
				// given
				var request = new PostUploadRequest(photoInfo, bubbleInfo, spotInfo, "디테일 주소", tagIds,
					mentionedUserIds,
					false);
				long rowNum = spotRepository.count();

				// when
				Long postId = postService.upload(writer.getId(), request).postId();
				var post = postRepository.findById(postId).orElseThrow();
				var response = postService.getPost(writer.getId(), postId);

				// then
				assertAll(
					() -> assertEquals(post.getLikeCount(), 0L),
					() -> assertEquals(post.getSpot().getPostCount(), 1L),
					() -> assertThat(response.photoUrl()).contains(S3Directory.POST_FOLDER.getPath())
						.doesNotContain(S3Directory.TEMP_FOLDER.getPath()),
					() -> assertThat(response.bubble().text()).isEqualTo("이미지 설명"),
					() -> assertThat(response.bubble().x()).isEqualTo(100),
					() -> assertThat(response.bubble().y()).isEqualTo(200),
					() -> assertThat(response.detailAddress()).isEqualTo("디테일 주소"),
					() -> assertThat(response.tags()).extracting("tagName").containsExactly("tagA", "tagB", "tagC"),
					() -> assertThat(response.mentions()).extracting("nickname").containsExactly("언급된 사용자"),
					() -> assertThat(response.isPrivate()).isFalse(),
					() -> assertThat(spotRepository.count()).isEqualTo(rowNum + 1));
			}), dynamicTest("스팟이 존재하면 기존 스팟에 방명록을 업로드한다.", () -> {
				// given
				spotRepository.save(createSpot(new CoordinateDto(37.51, 125.91)));
				var knownSpotInfo = new SpotInfoDto(new CoordinateDto(37.51, 125.91), "중점 좌표 기준 변환된 주소");
				var request = new PostUploadRequest(photoInfo, bubbleInfo, knownSpotInfo, "디테일 주소", tagIds,
					mentionedUserIds, false);
				long rowNum = spotRepository.count();

				// when
				postService.upload(writer.getId(), request);

				// then
				assertThat(spotRepository.count()).isEqualTo(rowNum);
			}), dynamicTest("이미지의 버블이 없는 경우 null을 저장한다.", () -> {
				// given
				var request = new PostUploadRequest(photoInfo, null, spotInfo, "디테일 주소", tagIds, mentionedUserIds,
					false);

				// when
				Long postId = postService.upload(writer.getId(), request).postId();
				var savedPost = postRepository.findById(postId).orElseThrow();

				// then
				assertThat(savedPost.getPhoto().getBubble()).isNull();
			}), dynamicTest("상세 주소에 공백만 존재하는 경우 null로 저장한다.", () -> {
				// given
				var request = new PostUploadRequest(photoInfo, bubbleInfo, spotInfo, "     ", null, null, false);

				// when
				Long postId = postService.upload(writer.getId(), request).postId();
				var post = postService.getPost(writer.getId(), postId);

				// then
				assertThat(post.detailAddress()).isNull();
			}), dynamicTest("존재하지 않는 태그가 포함되어 있으면 예외를 던진다.", () -> {
				// given
				var invalidTagIds = List.of(tags.get(0).getId(), tags.get(1).getId(), 3000L);
				var request = new PostUploadRequest(photoInfo, bubbleInfo, spotInfo, "디테일 주소", invalidTagIds,
					mentionedUserIds, false);

				// when & then
				assertThatThrownBy(() -> postService.upload(writer.getId(), request)).isInstanceOf(ApiException.class)
					.hasMessage(PostErrorCode.NOT_FOUND_TAG.getMessage());
			}), dynamicTest("존재하지 않는 사용자가 멘션되어 있으면 예외를 던진다.", () -> {
				// given
				var invalidUserIds = List.of(mentionedUser.getId(), 3000L);
				var request = new PostUploadRequest(photoInfo, bubbleInfo, spotInfo, "디테일 주소", tagIds, invalidUserIds,
					false);

				// when & then
				assertThatThrownBy(() -> postService.upload(writer.getId(), request)).isInstanceOf(ApiException.class)
					.hasMessage(UserErrorCode.NOT_FOUND_USER.getMessage());
			}));
	}

	@TestFactory
	@DisplayName("방명록 내용 수정 시나리오")
	Collection<DynamicTest> updatePost() {
		// given
		// given
		var photoInfo = new PhotoInfoDto("https://bucket.s3.ap-northeast-2.amazonaws.com/temp/example.webp",
			new CoordinateDto(35.512, 126.912), "2024-01-13T05:20:18.981+09:00");
		var bubbleInfo = new BubbleInfoDto("이미지 설명", 100, 200);
		var spotInfo = new SpotInfoDto(new CoordinateDto(35.557, 126.923), "중점 좌표 기준 변환된 주소");
		User writer = createUser("작성자");
		User mentionedUser1 = createUser("사용자1");
		User mentionedUser2 = createUser("사용자2");
		spotRepository.save(createSpot(new CoordinateDto(35.557, 126.923)));
		userRepository.saveAll(List.of(writer, mentionedUser1, mentionedUser2));
		List<Tag> tags = tagRepository.saveAll(createTags("tagA", "tagB", "tagC"));
		var tagIds = List.of(tags.get(0).getId(), tags.get(1).getId());
		var mentionedUserIds = List.of(mentionedUser1.getId(), mentionedUser2.getId());
		var uploadRequest = new PostUploadRequest(photoInfo, bubbleInfo, spotInfo, "디테일 주소", tagIds, mentionedUserIds,
			false);
		Long postId = postService.upload(writer.getId(), uploadRequest).postId();

		// when
		var prePost = postService.getPost(writer.getId(), postId);
		assertAll(() -> assertThat(prePost.mentions().size()).isEqualTo(2),
			() -> assertThat(prePost.tags().size()).isEqualTo(2));

		return List.of(dynamicTest("기존 방명록 내용을 수정하고 관련 엔티티를 업데이트 한다.", () -> {
			// given
			List<Long> newTagIds = List.of(tags.get(1).getId());
			List<Long> newMentionedUserIds = List.of(mentionedUser2.getId());
			var httpRequest = new PostUpdateHttpRequest(newTagIds, newMentionedUserIds, "새로운 상세 주소");
			var request = PostUpdateRequest.of(writer.getId(), postId, httpRequest);

			// when
			postService.update(request);
			var response = postService.getPost(writer.getId(), postId);

			// then
			assertAll(
				() -> assertThat(response.writer().nickname()).isEqualTo("작성자"),
				() -> assertFalse(response.isPrivate()),
				() -> assertThat(response.tags()).extracting("tagName").containsExactly("tagB"),
				() -> assertThat(response.mentions()).extracting("nickname").containsExactly("사용자2"),
				() -> assertThat(response.detailAddress()).isEqualTo("새로운 상세 주소"), () -> assertThat(
					postTagRepository.findAllByPostId(postId)
						.stream()
						.map(PostTag::getTag)
						.toList()).containsExactlyInAnyOrderElementsOf(List.of(tags.get(1))), () -> assertThat(
					mentionRepository.findAlLByPostId(postId)
						.stream()
						.map(Mention::getMentionedUser)
						.toList()).containsExactlyInAnyOrderElementsOf(List.of(mentionedUser2)));
		}), dynamicTest("수정하려는 상세 주소에 공백만 존재하는 경우 null을 저장한다.", () -> {
			// given
			var httpRequest = new PostUpdateHttpRequest(Collections.emptyList(), Collections.emptyList(), "       ");
			var request = PostUpdateRequest.of(writer.getId(), postId, httpRequest);

			// when
			postService.update(request);
			var response = postService.getPost(writer.getId(), postId);

			// then
			assertAll(() -> assertThat(response.tags()).isEqualTo(Collections.emptyList()),
				() -> assertThat(response.mentions()).isEqualTo(Collections.emptyList()),
				() -> assertNull(response.detailAddress()));
		}), dynamicTest("다른 사용자가 작성한 방명록을 수정할 경우 예외를 던진다.", () -> {
			// given
			var otherUser = createUser("다른 사용자");
			userRepository.save(otherUser);
			var httpRequest = new PostUpdateHttpRequest(Collections.emptyList(), Collections.emptyList(), "");
			var request = PostUpdateRequest.of(otherUser.getId(), postId, httpRequest);

			// when & then
			assertThatThrownBy(() -> postService.update(request)).isInstanceOf(ApiException.class)
				.hasMessage(AuthErrorCode.PERMISSION_DENIED.getMessage());
		}), dynamicTest("존재하지 않는 방명록을 수정할 경우 예외를 던진다.", () -> {
			// given
			var wrongPostId = 100L;
			var httpRequest = new PostUpdateHttpRequest(Collections.emptyList(), Collections.emptyList(), "");
			var request = PostUpdateRequest.of(writer.getId(), wrongPostId, httpRequest);

			// when & then
			assertThatThrownBy(() -> postService.update(request)).isInstanceOf(ApiException.class)
				.hasMessage(PostErrorCode.NOT_FOUND_POST.getMessage());
		}), dynamicTest("공개 방명록을 비공개로 변경한다.", () -> {
			// when
			postService.updatePrivacyState(writer.getId(), postId, true);
			var post = postService.getPost(writer.getId(), postId);

			// then
			assertTrue(post.isPrivate());
		}));
	}

	@DisplayName("태그 목록을 조회한다.")
	@Test
	void getTags() {
		//given
		tagRepository.saveAll(createTags("tag1", "tag2", "tag3"));
		//when
		final List<TagResponse> tagResponses = postService.getTags();
		//then
		assertThat(tagResponses).hasSize(3);
		assertThat(tagResponses).extracting("tagName")
			.contains("tag1", "tag2", "tag3");
	}

	@TestFactory
	@DisplayName("방명록 신고 시나리오")
	Stream<DynamicTest> reportPost() {
		// given
		User writer = createUser("작성자");
		User reporter = createUser("신고자");
		userRepository.saveAll(List.of(writer, reporter));
		Post post = createPost(spotRepository.save(createSpot()), writer);
		postRepository.save(post);

		return Stream.of(
			dynamicTest("방명록을 신고한다.", () -> {
				// when
				postService.report(reporter.getId(), post.getId(), "불쾌감을 유발하는 사진입니다.");
				Report report = reportRepository.findByPostId(post.getId()).orElseThrow();

				// then
				assertAll(
					() -> assertEquals(report.getReporter(), reporter),
					() -> assertEquals(report.getReason(), "불쾌감을 유발하는 사진입니다.")
				);
			}),
			dynamicTest("이미 신고한 방명록인 경우 예외를 던진다.", () -> {
				// when & then
				assertThatThrownBy(() -> postService.report(reporter.getId(), post.getId(), "타인을 비방하는 사진입니다."))
					.isInstanceOf(ApiException.class).hasMessage(PostErrorCode.ALREADY_REPORT.getMessage());
			}),
			dynamicTest("본인이 작성한 방명록을 신고하는 경우 예외를 던진다.", () -> {
				// when & then
				assertThatThrownBy(() -> postService.report(post.getWriter().getId(), post.getId(), "이상한 사진입니다."))
					.isInstanceOf(ApiException.class).hasMessage(PostErrorCode.CNA_NOT_REPORT_OWN_POST.getMessage());
			})
		);
	}

	@TestFactory
	@DisplayName("신고 방명록 목록 조회 시나리오")
	Stream<DynamicTest> getReportedPosts() {
		// given
		Spot spot = spotRepository.save(createSpot());
		User writer = userRepository.save(createUser("작성자"));
		User reporter = userRepository.save(createUser("신고자"));

		return Stream.of(
			dynamicTest("내가 신고한 방명록 목록을 조회한다.", () -> {
				// given
				Post post1 = postRepository.save(createPost(spot, writer));
				Post post2 = postRepository.save(createPost(spot, writer));
				reportRepository.save(post1.reportFrom(reporter, "불쾌한 사진"));
				reportRepository.save(post2.reportFrom(reporter, "징그러운 사진"));

				// when
				var myReports = postService.getReports(reporter.getId());

				// then
				assertAll(
					() -> assertThat(myReports).hasSize(2),
					() -> assertThat(myReports.stream().map(ReportResponse::postId).toList()).containsExactlyInAnyOrder(
						post1.getId(), post2.getId()),
					() -> assertThat(myReports).allMatch(report -> report.spotAddress().equals(spot.getAddress()))
				);
			}),
			dynamicTest("신고한 목록이 없으면 빈 리스트를 반환한다.", () -> {
				// when
				var emptyReports = postService.getReports(writer.getId());

				// then
				assertThat(emptyReports).isEmpty();
			})
		);
	}
}
