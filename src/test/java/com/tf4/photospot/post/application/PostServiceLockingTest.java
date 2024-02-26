package com.tf4.photospot.post.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.support.TransactionTemplate;

import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostLike;
import com.tf4.photospot.post.domain.PostLikeRepository;
import com.tf4.photospot.post.domain.PostRepository;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.spot.domain.SpotRepository;
import com.tf4.photospot.user.domain.User;
import com.tf4.photospot.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PostServiceLockingTest {
	private final PostService postService;
	private final PostRepository postRepository;
	private final SpotRepository spotRepository;
	private final UserRepository userRepository;
	private final PostLikeRepository postLikeRepository;
	private final TransactionTemplate transactionTemplate;

	@AfterEach
	void tearDown() {
		postLikeRepository.deleteAll();
		postRepository.deleteAll();
		spotRepository.deleteAll();
		userRepository.deleteAll();
	}

	@DisplayName("비슷한 타이밍에 방명록 좋아요를 누르면 낙관적 락 예외가 발생한다.")
	@Test
	void postLikeOptimisticLockExceptionTest() {
		//given
		final Spot spot = spotRepository.save(createSpot());
		final User writer = userRepository.save(createUser("이성빈"));
		final Post post = postRepository.save(createPost(spot, writer, 0L));
		final User likeFailUser = userRepository.save(createUser("userA"));
		final User likeSuccessUser = userRepository.save(createUser("userB"));
		var beforeVersion = post.getVersion();
		var beforeLikeCount = post.getLikeCount();
		var waitingLatch = new CountDownLatch(1);
		//when
		// 낙관적 락 version이 업데이트가 될때까지 대기
		var waitWorker = CompletableFuture.runAsync(() -> likePostWaitWithNoRetry(post, likeFailUser, waitingLatch));
		sleep(200L);
		// 낙관적 락 version을 업데이트하고 countDown
		CompletableFuture.runAsync(() -> postService.likePost(post.getId(), likeSuccessUser.getId()))
			.thenRun(waitingLatch::countDown);
		// then
		// 두번째 작업의 완료로 인해 version이 변경 되어 OptimisticLockingFailureException 예외 발생
		waitWorker.exceptionally(ex -> {
			assertThat(ex.getCause()).isInstanceOf(OptimisticLockingFailureException.class);
			return null;
		}).join();
		assertThat(postRepository.findById(post.getId())).isPresent().get()
			.extracting("version", "likeCount")
			.containsExactly(beforeVersion + 1, beforeLikeCount + 1L);
	}

	@DisplayName("방명록 좋아요 동시성 테스트")
	@TestFactory
	Stream<DynamicTest> postLikeLockTest() {
		//given
		final int postLikeCount = 2;
		final Spot spot = spotRepository.save(createSpot());
		final User writer = userRepository.save(createUser("이성빈"));
		final Post post = postRepository.save(createPost(spot, writer, 0L));
		final List<User> users = userRepository.saveAll(createList(() -> createUser("user"), postLikeCount));
		final ExecutorService executorService = Executors.newFixedThreadPool(postLikeCount);

		return Stream.of(
			dynamicTest("여러 유저가 좋아요를 동시에 누르면 retry로 성공한다.", () -> {
				//given
				final CountDownLatch completeLatch = new CountDownLatch(postLikeCount);
				//when
				users.forEach(user -> executorService.submit(() -> {
					postService.likePost(post.getId(), user.getId());
					completeLatch.countDown();
				}));
				completeLatch.await();
				//then
				assertThat(postRepository.findById(post.getId())).isPresent().get()
					.satisfies(updatedPost -> assertThat(updatedPost.getLikeCount()).isEqualTo(postLikeCount));
			}),
			dynamicTest("여러 유저가 좋아요 취소를 동시에 누르면 retry로 성공한다.", () -> {
				//given
				final CountDownLatch completeLatch = new CountDownLatch(postLikeCount);
				//when
				users.forEach(user -> executorService.submit(() -> {
					postService.cancelPostLike(post.getId(), user.getId());
					completeLatch.countDown();
				}));
				completeLatch.await();
				//then
				assertThat(postRepository.findById(post.getId())).isPresent().get()
					.satisfies(updatedPost -> assertThat(updatedPost.getLikeCount()).isZero());
			})
		);
	}

	void likePostWaitWithNoRetry(Post post, User user, CountDownLatch waitingLatch) {
		transactionTemplate.executeWithoutResult(status -> {
			PostLike postLike = post.likeFrom(user);
			wait(waitingLatch);
			postLikeRepository.save(postLike);
		});
	}

	private static void wait(CountDownLatch waitingLatch) {
		try {
			waitingLatch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void sleep(Long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
