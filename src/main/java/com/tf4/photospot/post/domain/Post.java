package com.tf4.photospot.post.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.AuthErrorCode;
import com.tf4.photospot.global.exception.domain.PostErrorCode;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	indexes = @Index(name = "post_search_idx",
		columnList = "spot_id, is_private, deleted_at, photo_id")
)
public class Post extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User writer;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "photo_id")
	private Photo photo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "spot_id")
	private Spot spot;

	@OneToMany(mappedBy = "post")
	private List<PostTag> postTags = new ArrayList<>();

	@OneToMany(mappedBy = "post")
	private List<Mention> mentions = new ArrayList<>();

	private String detailAddress;

	private Long likeCount;

	private boolean isPrivate;

	private LocalDateTime deletedAt;

	@Version
	private Integer version;

	@Builder
	public Post(User writer, Photo photo, Spot spot, String detailAddress, Long likeCount, boolean isPrivate) {
		this.writer = writer;
		this.photo = photo;
		this.spot = spot;
		this.detailAddress = detailAddress;
		this.likeCount = likeCount == null ? 0L : likeCount;
		this.isPrivate = isPrivate;
	}

	public void delete(User loginUser) {
		checkWriter(loginUser);
		if (deletedAt == null) {
			deletedAt = LocalDateTime.now();
			getPhoto().delete();
			getSpot().decPostCount();
		}
	}

	public PostLike likeFrom(User user) {
		likeCount++;
		return new PostLike(this, user);
	}

	public void cancelLike(PostLike postLike) {
		if (this != postLike.getPost() || likeCount == 0L) {
			throw new ApiException(PostErrorCode.CAN_NOT_CANCEL_LIKE);
		}
		likeCount--;
	}

	public void updateDetailAddress(User loginUser, String detailAddress) {
		checkWriter(loginUser);
		this.detailAddress = detailAddress;
	}

	public void updatePrivacyState(User loginUser, boolean status) {
		checkWriter(loginUser);
		this.isPrivate = status;
	}

	public boolean isWriter(User user) {
		return this.writer.equals(user);
	}

	private void checkWriter(User user) {
		if (!isWriter(user)) {
			throw new ApiException(AuthErrorCode.PERMISSION_DENIED);
		}
	}

	public Report reportFrom(User reporter, String reason) {
		return new Report(reporter, this, reason);
	}

	public String getPhotoUrl() {
		return photo.getPhotoUrl();
	}
}
