package com.tf4.photospot.photo.domain;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String photoUrl;

	private LocalDateTime takenAt;

	@Column(columnDefinition = "POINT SRID 4326", nullable = false)
	private Point coord;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "bubble_id")
	private Bubble bubble;

	private LocalDateTime deletedAt;

	@Builder
	public Photo(String photoUrl, LocalDateTime takenAt, Point coord, Bubble bubble) {
		this.photoUrl = photoUrl;
		this.takenAt = takenAt;
		this.coord = coord;
		this.bubble = bubble;
	}

	public void delete() {
		if (deletedAt == null) {
			deletedAt = LocalDateTime.now();
		}
	}

	public void updatePhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
}
