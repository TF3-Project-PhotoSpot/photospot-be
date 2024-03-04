package com.tf4.photospot.bookmark.domain;

import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.BookmarkErrorCode;
import com.tf4.photospot.spot.domain.Spot;
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
	public static final int MAX_BOOKMARKED = 200;

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

	public Bookmark add(CreateBookmark createBookmark, Spot spot) {
		verifyBookmarkAddition(createBookmark.userId());
		++totalCount;
		return createBookmark.create(this, spot);
	}

	private void verifyBookmarkAddition(Long userId) {
		if (totalCount == MAX_BOOKMARKED) {
			throw new ApiException(BookmarkErrorCode.MAX_BOOKMARKED);
		}
		if (!user.isSame(userId)) {
			throw new ApiException(BookmarkErrorCode.NO_AUTHORITY_BOOKMARK_FOLDER);
		}
	}
}
