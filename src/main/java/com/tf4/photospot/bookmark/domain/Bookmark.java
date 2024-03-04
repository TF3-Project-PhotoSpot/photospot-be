package com.tf4.photospot.bookmark.domain;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.spot.domain.Spot;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "spot_id")
	private Spot spot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookmark_folder_id")
	private BookmarkFolder bookmarkFolder;

	private String name;

	private String description;

	@Builder
	public Bookmark(Spot spot, BookmarkFolder bookmarkFolder, String name, String description) {
		this.spot = spot;
		this.bookmarkFolder = bookmarkFolder;
		this.name = name;
		this.description = description;
	}
}
