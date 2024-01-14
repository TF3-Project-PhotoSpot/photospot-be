package com.tf4.photospot.photo.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

	List<Photo> findAllByCreatedAtGreaterThanEqual(LocalDateTime startDateTime);
}
