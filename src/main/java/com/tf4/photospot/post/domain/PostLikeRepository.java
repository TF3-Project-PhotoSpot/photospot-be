package com.tf4.photospot.post.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
	boolean existsByPostIdAndUserId(Long postId, Long userId);
}
