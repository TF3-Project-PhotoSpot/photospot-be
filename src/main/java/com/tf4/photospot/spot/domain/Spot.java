package com.tf4.photospot.spot.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.post.domain.Post;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spot extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "spot")
	private List<Post> posts = new ArrayList<>();

	private String address;

	private Point coord;

	private Long postCount;

	private LocalDateTime deletedAt;

	public Spot(String address, Point coord, Long postCount) {
		this.address = address;
		this.coord = coord;
		this.postCount = postCount;
		this.deletedAt = LocalDateTime.now();
	}
}
