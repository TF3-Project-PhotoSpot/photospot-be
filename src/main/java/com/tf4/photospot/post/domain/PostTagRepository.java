package com.tf4.photospot.post.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
	List<PostTag> findAllByPostId(Long postId);

	void deleteByPostId(Long postId);
}
