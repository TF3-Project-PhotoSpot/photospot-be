package com.tf4.photospot.spot.domain;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.PostErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
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

	private String address;

	@Column(columnDefinition = "POINT SRID 4326", nullable = false)
	private Point coord;

	private Long postCount;

	private LocalDateTime deletedAt;

	@Builder
	public Spot(String address, Point coord, Long postCount) {
		this.address = address;
		this.coord = coord;
		this.postCount = postCount == null ? 0L : postCount;
	}

	public void incPostCount() {
		postCount++;
	}

	public void decPostCount() {
		if (postCount == 0L) {
			throw new ApiException(PostErrorCode.NOT_FOUND_POST);
		}
		postCount--;
	}
}
