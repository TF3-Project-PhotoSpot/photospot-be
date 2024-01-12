package com.tf4.photospot.photo.domain;

import com.tf4.photospot.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
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

	@OneToOne
	@JoinColumn(name = "bubble_id")
	private Bubble bubble;

	public Photo(String photoUrl, Bubble bubble) {
		this.photoUrl = photoUrl;
		this.bubble = bubble;
	}

	public Photo(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public void updatePhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}
}
