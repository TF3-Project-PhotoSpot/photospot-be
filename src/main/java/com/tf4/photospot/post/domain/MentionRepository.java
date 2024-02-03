package com.tf4.photospot.post.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MentionRepository extends JpaRepository<Mention, Long> {

	List<Mention> findByPostId(Long postId);
}
