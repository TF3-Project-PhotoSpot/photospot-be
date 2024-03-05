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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	indexes = @Index(name = "report_unique_idx", columnList = "user_id, post_id", unique = true)
)
public class Report extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User reporter;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	private String reason;

	@Builder
	public Report(User reporter, Post post, String reason) {
		this.reporter = reporter;
		this.post = post;
		this.reason = reason;
	}
}
