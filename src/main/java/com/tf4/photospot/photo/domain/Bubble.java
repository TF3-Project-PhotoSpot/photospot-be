package com.tf4.photospot.photo.domain;

import com.tf4.photospot.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bubble extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String text;

	private long posX;

	private long posY;

	@Builder
	public Bubble(String text, long posX, long posY) {
		this.text = text;
		this.posX = posX;
		this.posY = posY;
	}
}
