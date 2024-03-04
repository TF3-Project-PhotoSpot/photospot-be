package com.tf4.photospot.bookmark.domain;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.user.domain.User;

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
public class BookmarkFolder extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	private String name;

	private String description;

	private String color;

	private int totalCount;

	@Builder
	public BookmarkFolder(User user, String name, String description, String color, int totalCount) {
		this.user = user;
		this.name = name;
		this.description = description;
		this.color = color;
		this.totalCount = totalCount;
	}

	public static BookmarkFolder createDefaultBookmark(User user) {
		return BookmarkFolder.builder()
			.user(user)
			.name("내 장소")
			.build();
	}
}
