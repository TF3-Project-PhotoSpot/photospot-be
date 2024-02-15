package com.tf4.photospot.post.domain;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.user.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	indexes = @Index(name = "post_like_unique_idx", columnList = "post_id, user_id", unique = true)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Builder
	public PostLike(Post post, User user) {
		this.post = post;
		this.user = user;
	}

	public void cancel() {
		post.cancelLike(this);
	}
}
