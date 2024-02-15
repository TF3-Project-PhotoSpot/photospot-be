package com.tf4.photospot.post.application;

import static com.tf4.photospot.support.TestFixture.*;
import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostSearchType;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostLikeRepository;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.post.domain.PostTagRepository;
import com.tf4.photospot.post.domain.Tag;
import com.tf4.photospot.post.domain.TagRepository;
import com.tf4.photospot.post.presentation.request.PhotoInfoDto;
import com.tf4.photospot.post.presentation.request.PostUploadHttpRequest;
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
				assertThat(postRepository.findById(post.getId())).isPresent().get()
					.satisfies(updatedPost -> assertThat(updatedPost.getLikeCount()).isEqualTo(beforeLikes + 1));
			}),
			dynamicTest("좋아요를 중복해서 할 수 없다.", () ->
				assertThatThrownBy(() -> postService.likePost(post.getId(), user.getId()))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(PostErrorCode.ALREADY_LIKE)
			),
			dynamicTest("좋아요 취소를 할 수 있다.", () -> {
				//given
				final Long beforeLikes = post.getLikeCount();
				//when
				postService.cancelPostLike(post.getId(), user.getId());
				//then
				assertThat(postRepository.findById(post.getId())).isPresent().get()
					.satisfies(updatedPost -> assertThat(updatedPost.getLikeCount()).isEqualTo(beforeLikes - 1));
			}),
			dynamicTest("좋아요 취소를 중복해서 할 수 없다.", () ->
				assertThatThrownBy(() -> postService.cancelPostLike(post.getId(), user.getId()))
					.isInstanceOf(ApiException.class)
					.extracting("errorCode")
					.isEqualTo(PostErrorCode.NO_EXISTS_LIKE)
			)
		);
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

		return Stream.of(
			dynamicTest("슬라이스 페이징으로 조회한다.", () -> {
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
			}),
			dynamicTest("좋아요순으로 조회할 수 있다.", () -> {
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
				assertThatList(response.content().stream().map(PostPreviewResponse::postId).toList())
					.isEqualTo(postIdsSortedLikeCountDesc);
			}),
			dynamicTest("삭제 되었거나 비공개 방명록은 조회할 수 없다.", () -> {
				//given
				Post privatePost = createPost(spot, writer, true);
				Post deletePost = createPost(spot, writer);
				deletePost.delete();
				postRepository.saveAll(List.of(privatePost, deletePost));

				var latestPostRequest = PostSearchCondition.builder()
					.spotId(spot.getId())
					.userId(reader.getId())
					.type(PostSearchType.POSTS_OF_SPOT)
					.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
					.build();
				//when
				SlicePageDto<PostPreviewResponse> response = postService.getPostPreviews(latestPostRequest);
				//then
				assertThat(response.content().get(0).postId()).isNotIn(privatePost.getId(), deletePost.getId());
			}),
			dynamicTest("내 방명록만 조회 시 다른 유저가 작성한 방명록은 볼 수 없다.", () -> {
				//given
				final Post otherUserPost = postRepository.save(createPost(spot, reader, false));
				final PostSearchCondition searchCondition = PostSearchCondition.builder()
					.userId(writer.getId())
					.type(PostSearchType.MY_POSTS)
					.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
					.build();
				//when
				final boolean visibleOtherWriterPost = postService.getPostPreviews(searchCondition).content()
					.stream()
					.anyMatch(postPreview -> postPreview.postId().equals(otherUserPost.getId()));
				//then
				assertFalse(visibleOtherWriterPost);
			}),
			dynamicTest("내 방명록 조회중 삭제된 것은 볼 수 없고 비공개는 볼 수 있다.", () -> {
				//given
				final Post privatePost = postRepository.save(createPost(spot, writer, true));
				final Post deletePost = createPost(spot, writer, false);
				deletePost.delete();
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
			}),
			dynamicTest("내가 좋아요한 방명록이 없는 경우 조회 결과가 나오지 않는다.", () -> {
				final User user = userRepository.save(createUser("user"));
				final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
					.userId(user.getId())
					.type(PostSearchType.LIKE_POSTS)
					.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
					.build();
				var response = postService.getPostPreviews(postSearchCondition);
				assertThat(response.content()).isEmpty();
				assertThat(response.hasNext()).isFalse();
			}),
			dynamicTest("내가 좋아요한 방명록 미리보기 목록을 조회할 수 있다.", () -> {
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
			}),
			dynamicTest("내가 좋아요한 방명록 목록을 좋아요한 순서로 조회 수 있다.", () -> {
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
				assertThat(response.content())
					.extracting(PostPreviewResponse::postId)
					.containsExactly(
						like3.getPost().getId(),
						like2.getPost().getId(),
						like1.getPost().getId());
			})
		);
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
		// 마지막에 추가된 포스트는 태그, 좋아요 정보를 가지고 있다.
		Post lastPost = createPost(spot, writer, createPhoto("firstPhotoUrl"));
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

		return Stream.of(
			dynamicTest("방명록 상세 목록을 슬라이스 페이징으로 조회한다.", () -> {
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
				assertThatList(postsSortedByOldest.content()).isSortedAccordingTo(
					comparing(PostDetailResponse::id));
			}),
			dynamicTest("좋아요순으로 조회할 수 있다.", () -> {
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
			}),
			dynamicTest("좋아요순으로, 좋아요가 같으면 최신순으로 조회할 수 있다.", () -> {
				//given
				final Long mostLikeCount = 1000L;
				final PostSearchCondition searchCondition = PostSearchCondition.builder()
					.spotId(spot.getId())
					.userId(reader.getId())
					.type(PostSearchType.POSTS_OF_SPOT)
					.pageable(PageRequest.of(0, 10, Sort.by(
						Sort.Order.desc("likeCount"),
						Sort.Order.desc("id"))))
					.build();
				//when
				postRepository.saveAll(createList(() -> createPost(spot, writer, mostLikeCount), 5));
				SlicePageDto<PostDetailResponse> postsSortedByMostLikeCountAndLatest = postService.getPosts(
					searchCondition);
				//then
				assertThatList(postsSortedByMostLikeCountAndLatest.content())
					.isSortedAccordingTo(comparing(PostDetailResponse::likeCount).reversed()
						.thenComparing(comparing(PostDetailResponse::id).reversed()));
			}),
			dynamicTest("내 방명록 상세 조회 시 다른 유저가 작성한 방명록은 볼 수 없다.", () -> {
				//given
				final Post otherUserPost = postRepository.save(createPost(spot, reader, false));
				final PostSearchCondition searchCondition = PostSearchCondition.builder()
					.userId(writer.getId())
					.type(PostSearchType.MY_POSTS)
					.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
					.build();
				//when
				final boolean visibleOtherWriterPost = postService.getPosts(searchCondition).content()
					.stream()
					.anyMatch(postDetailResponse -> postDetailResponse.id().equals(otherUserPost.getId()));
				//then
				assertFalse(visibleOtherWriterPost);
			}),
			dynamicTest("내 방명록 상세 조회시 삭제된 것은 볼 수 없고 비공개는 볼 수 있다.", () -> {
				//given
				final Post privatePost = postRepository.save(createPost(spot, writer, true));
				final Post deletePost = createPost(spot, writer, false);
				deletePost.delete();
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
			}),
			dynamicTest("내가 좋아요한 방명록이 없는 경우 상세 조회 결과가 나오지 않는다.", () -> {
				final User user = userRepository.save(createUser("user"));
				final PostSearchCondition postSearchCondition = PostSearchCondition.builder()
					.userId(user.getId())
					.type(PostSearchType.LIKE_POSTS)
					.pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
					.build();
				var response = postService.getPosts(postSearchCondition);
				assertThat(response.content()).isEmpty();
				assertThat(response.hasNext()).isFalse();
			}),
			dynamicTest("내가 좋아요한 방명록 상세 목록을 조회할 수 있다.", () -> {
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
			}),
			dynamicTest("내가 좋아요한 방명록 상세 목록을 좋아요한 순서로 조회 수 있다.", () -> {
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
				assertThat(response.content())
					.extracting(PostDetailResponse::id)
					.containsExactly(
						like3.getPost().getId(),
						like2.getPost().getId(),
						like1.getPost().getId());
			})
		);
	}

	@TestFactory
	@DisplayName("방명록 등록 시나리오")
	Collection<DynamicTest> uploadPost() {
		// given
		var spotCoord = new CoordinateDto(35.557, 126.923);
		User writer = createUser("작성자");
		User mentionedUser = createUser("언급된 사용자");
		Spot spot = spotRepository.save(createSpot(spotCoord));
		userRepository.saveAll(List.of(writer, mentionedUser));
		List<Tag> tags = tagRepository.saveAll(createTags("tagA", "tagB", "tagC"));
		var spotInfo = new SpotInfoDto(spotCoord, "주소");
		var photoInfo = new PhotoInfoDto("https://bucket.s3.ap-northeast-2.amazonaws.com/temp/example.webp",
			new CoordinateDto(35.512, 126.912), "2024-01-13T05:20:18.981+09:00");
		var tagIds = List.of(tags.get(0).getId(), tags.get(1).getId(), tags.get(2).getId());
		var mentionedUserIds = List.of(mentionedUser.getId());

		return List.of(
			dynamicTest("방명록을 업로드하고 그와 관련된 엔티티를 저장한다.", () -> {
				// given
				var httpRequest = new PostUploadHttpRequest(photoInfo, spotInfo, "할리스", tagIds, mentionedUserIds,
					false);
				var request = PostUploadRequest.of(writer.getId(), httpRequest);

				// when
				long rowNum = spotRepository.count();
				Post post = postRepository.findById(postService.upload(request).postId()).orElseThrow();

				// then
				assertAll(
					() -> assertThat(post.getPhoto().getPhotoUrl()).contains(S3Directory.POST_FOLDER.getPath())
						.doesNotContain(S3Directory.TEMP_FOLDER.getPath()),
					() -> assertThat(post.getDetailAddress()).isEqualTo("할리스"),
					() -> assertThat(post.getSpot()).isEqualTo(spot),
					() -> assertThat(post.isPrivate()).isFalse(),
					() -> assertThat(spotRepository.count()).isEqualTo(rowNum)
				);
			}),
			dynamicTest("스팟이 존재하지 않으면 스팟을 생성하고 방명록을 업로드한다.", () -> {
				// given
				var newSpotInfo = new SpotInfoDto(new CoordinateDto(36.5, 125.5), "새로운 주소");
				var httpRequest = new PostUploadHttpRequest(photoInfo, newSpotInfo, "풍경이 예쁜 곳", tagIds,
					mentionedUserIds, false);
				var request = PostUploadRequest.of(writer.getId(), httpRequest);
				long rowNum = spotRepository.count();

				// when
				Post post = postRepository.findById(postService.upload(request).postId()).orElseThrow();

				// then
				assertAll(
					() -> assertThat(post.getSpot()).isEqualTo(
						spotRepository.findByCoord(newSpotInfo.coord().toCoord()).orElseThrow()),
					() -> assertThat(spotRepository.count()).isEqualTo(rowNum + 1)
				);
			}),
			dynamicTest("상세 주소에 공백만 존재하는 경우 null로 저장한다.", () -> {
				// given
				var httpRequest = new PostUploadHttpRequest(photoInfo, spotInfo, "     ", null, null, false);
				var request = PostUploadRequest.of(writer.getId(), httpRequest);

				// when
				Post post = postRepository.findById(postService.upload(request).postId()).orElseThrow();

				// then
				assertThat(post.getDetailAddress()).isNull();
			}),
			dynamicTest("존재하지 않는 태그가 포함되어 있으면 예외를 던진다.", () -> {
				// given
				var invalidTagIds = List.of(tags.get(0).getId(), tags.get(1).getId(), 3000L);
				var httpRequest = new PostUploadHttpRequest(photoInfo, spotInfo, "할리스", invalidTagIds,
					mentionedUserIds, false);
				var request = PostUploadRequest.of(writer.getId(), httpRequest);

				// when & then
				assertThatThrownBy(() -> postService.upload(request)).isInstanceOf(ApiException.class)
					.hasMessage(PostErrorCode.NOT_FOUND_TAG.getMessage());
			}),
			dynamicTest("존재하지 않는 사용자가 멘션되어 있으면 예외를 던진다.", () -> {
				// given
				var invalidUserIds = List.of(mentionedUser.getId(), 3000L);
				var httpRequest = new PostUploadHttpRequest(photoInfo, spotInfo, "할리스", tagIds,
					invalidUserIds, false);
				var request = PostUploadRequest.of(writer.getId(), httpRequest);

				// when & then
				assertThatThrownBy(() -> postService.upload(request)).isInstanceOf(ApiException.class)
					.hasMessage(UserErrorCode.NOT_FOUND_USER.getMessage());
			})
		);
	}
}
