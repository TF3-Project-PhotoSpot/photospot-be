package com.tf4.photospot.spot.infrastructure;

import static com.tf4.photospot.post.domain.QPost.*;
import static com.tf4.photospot.spot.domain.QSpot.*;
import static com.tf4.photospot.spot.domain.QSpotBookmark.*;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.spatial.GeometryExpressions;
import com.querydsl.spatial.SpatialOps;
import com.tf4.photospot.global.util.PageUtils;
import com.tf4.photospot.spot.application.response.QSpotCoordResponse;
import com.tf4.photospot.spot.application.response.SpotCoordResponse;
import com.tf4.photospot.spot.domain.Spot;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SpotQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Slice<Spot> searchRecommendedSpots(Point coord, Integer radius, Pageable pageable) {
		List<Spot> recommendedSpots = queryFactory
			.selectFrom(spot)
			.where(
				containsInRadius(coord, radius),
				canVisible()
			)
			.orderBy(spot.postCount.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1L)
			.fetch();

		return PageUtils.toSlice(pageable, recommendedSpots);
	}

	private static BooleanExpression canVisible() {
		return spot.postCount.gt(0L).and(spot.deletedAt.isNull());
	}

	/*
	 * 	SQL : st_contains(st_buffer(:coord, :radius), spot.coord)
	 * 	st_buffer
	 * 		- 공간 범위를 그리는 함수로 중심 좌표에서부터 반경(radius)만큼의 범위를 그립니다.
	 * 		- 기본 설정은 32개의 점으로 원 모양의 다각형(Polygon)을 표현합니다.
	 * 	st_contains
	 * 		- 첫번째 공간 타입 내에 두번째 공간 타입이 포함이 되는지 여부를 확인합니다.
	 * 		- st_buffer로 만들어낸 범위 내에 포함 되는 좌표들을 찾는 함수입니다.
	 * */
	private static BooleanExpression containsInRadius(Point coord, Integer radius) {
		return Expressions.booleanOperation(SpatialOps.CONTAINS,
			GeometryExpressions.geometryOperation(SpatialOps.BUFFER,
				Expressions.constant(coord),
				Expressions.asNumber(radius)
			), spot.coord);
	}

	public Boolean existsBookmark(Long spotId, Long userId) {
		Integer exists = queryFactory.selectOne()
			.from(spotBookmark)
			.where(spotBookmark.spot.id.eq(spotId).and(spotBookmark.user.id.eq(userId)))
			.fetchFirst();
		return exists != null;
	}

	public List<SpotCoordResponse> findSpotsOfMyPosts(Long userId) {
		return queryFactory.selectDistinct(new QSpotCoordResponse(
				spot.id,
				spot.coord))
			.from(spot)
			.join(post).on(post.spot.eq(spot))
			.where(post.writer.id.eq(userId))
			.fetch();
	}
}
