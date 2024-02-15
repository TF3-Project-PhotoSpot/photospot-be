package com.tf4.photospot.post.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

	void deleteByPostId(Long postId);
}
