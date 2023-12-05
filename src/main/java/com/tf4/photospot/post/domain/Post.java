package com.tf4.photospot.post.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	private Point coord;

	private String detailAddress;

	private Long likeCount;

	private boolean isPrivate;

	private LocalDateTime deletedAt;

	@Builder
	public Post(User writer, Photo photo, Spot spot, Point coord, String detailAddress, Long likeCount,
		boolean isPrivate) {
		this.writer = writer;
		this.photo = photo;
		this.spot = spot;
		this.coord = coord;
		this.detailAddress = detailAddress;
		this.likeCount = likeCount;
		this.isPrivate = isPrivate;
	}
}
