package com.tf4.photospot.post.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
	Optional<Report> findByPostId(Long postId);
}
