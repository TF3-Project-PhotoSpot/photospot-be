package com.tf4.photospot.spot.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.post.domain.Post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = {
	@Index(name = "coord_idx", columnList = "coord")}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spot extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToMany(mappedBy = "spot")
	private List<Post> posts = new ArrayList<>();

	private String address;

	@Column(columnDefinition = "POINT SRID 4326", nullable = false)
	private Point coord;

	private Long postCount;

	private LocalDateTime deletedAt;

	public Spot(String address, Point coord) {
		this.address = address;
		this.coord = coord;
		postCount = 0L;
	}
}
