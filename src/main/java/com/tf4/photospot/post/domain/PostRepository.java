package com.tf4.photospot.post.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	boolean existsByPhotoId(Long photoId);
}
