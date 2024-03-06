package com.tf4.photospot.bookmark.infrastructure;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.util.QueryDslUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookmarkQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;
}
