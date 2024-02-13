package com.tf4.photospot.global.util;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

public abstract class QueryDslUtils extends PageUtils {
	public <T> QueryDslUtils orderBy(
		JPAQuery<T> query,
		EntityPathBase<?> qEntity,
		Pageable pageable
	) {
		for (Sort.Order order : pageable.getSort()) {
			query.orderBy(toOrderSpecifier(qEntity, order));
		}
		return this;
	}

	public <T> Slice<T> toSlice(
		JPAQuery<T> query,
		Pageable pageable
	) {
		final List<T> contents = query.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1L)
			.fetch();
		return toSlice(pageable, contents);
	}

	@SuppressWarnings({"rawtypes, unchecked"})
	private <T> OrderSpecifier toOrderSpecifier(EntityPathBase<T> qEntity, Sort.Order order) {
		return new OrderSpecifier(getDirection(order),
			new PathBuilder<>(qEntity.getType(), qEntity.getMetadata()).get(order.getProperty()));
	}

	private Order getDirection(Sort.Order order) {
		if (order.isAscending()) {
			return Order.ASC;
		}
		return Order.DESC;
	}
}
