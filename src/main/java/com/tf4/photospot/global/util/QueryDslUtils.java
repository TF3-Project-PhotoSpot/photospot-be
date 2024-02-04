package com.tf4.photospot.global.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.tf4.photospot.post.domain.QPost;

public abstract class QueryDslUtils {
	public static <T> JPAQuery<T> orderBy(
		JPAQuery<T> query,
		QPost qEntity,
		Pageable pageable
	) {
		for (Sort.Order order : pageable.getSort()) {
			query.orderBy(toOrderSpecifier(qEntity, order));
		}
		return query;
	}

	@SuppressWarnings({"rawtypes, unchecked"})
	private static <T> OrderSpecifier toOrderSpecifier(EntityPathBase<T> qEntity, Sort.Order order) {
		return new OrderSpecifier(getDirection(order),
			new PathBuilder<>(qEntity.getType(), qEntity.getMetadata()).get(order.getProperty()));
	}

	private static Order getDirection(Sort.Order order) {
		if (order.isAscending()) {
			return Order.ASC;
		}
		return Order.DESC;
	}
}
