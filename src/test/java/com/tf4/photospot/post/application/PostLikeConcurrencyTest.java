package com.tf4.photospot.post.application;

import static com.tf4.photospot.support.TestFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.global.exception.domain.PostErrorCode;
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
@Import(PostLikeConcurrencyTest.NewTransaction.class)
public class PostLikeConcurrencyTest extends IntegrationTestSupport {
	private final PostService postService;
	private final SpotRepository spotRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final NewTransaction newTransaction;
	private final EntityManager em;

	private int totalCount;
	private List<User> users;
	private Spot spot;
	private Post post;

	@BeforeEach
	void setUp() {
		newTransaction.run(() -> {
			totalCount = 3;
			users = userRepository.saveAll(createList(() -> createUser("user"), totalCount));
			spot = spotRepository.save(createSpot());
			post = postRepository.save(createPost(spot, users.get(0)));
		});
	}

	@AfterEach
	void tearDown() {
		newTransaction.run(() -> {
			em.createQuery("delete from PostLike p where p.post.id = :postId")
				.setParameter("postId", post.getId()).executeUpdate();
			postRepository.delete(post);
			userRepository.deleteAll(users);
			spotRepository.delete(spot);
		});
	}

	@DisplayName("여러명이 방명록 좋아요를 눌러도 개수가 정확히 업데이트 된다.")
	@Test
	void verifyPostLikeConcurrency() throws InterruptedException {
		//given
		final CountDownLatch countDownLatch = new CountDownLatch(totalCount);
		final ExecutorService executorService = Executors.newFixedThreadPool(totalCount);

		//when
		users.forEach(user -> executorService.submit(() -> {
			postService.likePost(post.getId(), user.getId());
			countDownLatch.countDown();
		}));
		countDownLatch.await(10L, TimeUnit.SECONDS);

		//then
		assertThat(postRepository.findById(post.getId()))
			.isPresent().get()
			.extracting("likeCount")
			.isEqualTo(totalCount);
	}

	@DisplayName("좋아요 따닥 요청이 오면 하나만 처리가 된다.")
	@Test
	void verifyPostLikeDoubleRequest() throws InterruptedException {
		//given
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		final User user = users.get(0);
		final int beforeLikeCount = post.getLikeCount();

		//when
		executorService.submit(() -> {
			try {
				postService.likePost(post.getId(), user.getId());
			} catch (Exception ignored) {
			}
			countDownLatch.countDown();
		});
		executorService.submit(() -> {
			try {
				postService.likePost(post.getId(), user.getId());
			} catch (Exception ignored) {
			}
			countDownLatch.countDown();
		});
		countDownLatch.await(10L, TimeUnit.SECONDS);

		//then
		assertThat(postRepository.findById(post.getId()))
			.isPresent().get()
			.extracting("likeCount")
			.isEqualTo(beforeLikeCount + 1);
	}

	@DisplayName("좋아요 취소 따닥 요청이 오면 나머지 요청은 NO_EXISTS_LIKE 예외가 발생한다.")
	@Test
	void verifyPostLikeCancelDoubleRequest() throws InterruptedException {
		//given
		final CountDownLatch countDownLatch = new CountDownLatch(2);
		final ExecutorService executorService = Executors.newFixedThreadPool(2);
		final User user = users.get(0);
		newTransaction.run(() -> postService.likePost(post.getId(), user.getId()));
		AtomicReference<Exception> exceptionHolder = new AtomicReference<>();
		//when
		executorService.submit(() -> {
			try {
				postService.cancelPostLike(post.getId(), user.getId());
			} catch (Exception ex) {
				exceptionHolder.set(ex);
			}
			countDownLatch.countDown();
		});
		executorService.submit(() -> {
			try {
				postService.cancelPostLike(post.getId(), user.getId());
			} catch (Exception ex) {
				exceptionHolder.set(ex);
			}
			countDownLatch.countDown();
		});
		countDownLatch.await(10L, TimeUnit.SECONDS);

		//then
		assertThat(postRepository.findById(post.getId()))
			.isPresent().get()
			.extracting("likeCount")
			.isEqualTo(0);
		assertThat(exceptionHolder.get()).isNotNull()
			.extracting("errorCode")
			.isEqualTo(PostErrorCode.NO_EXISTS_LIKE);
	}

	@Component
	static class NewTransaction {
		@Transactional(propagation = Propagation.REQUIRES_NEW)
		public void run(Runnable runnable) {
			runnable.run();
		}
	}
}
