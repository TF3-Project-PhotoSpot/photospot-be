package com.tf4.photospot.user.infrastructure;

import static com.tf4.photospot.user.domain.QUser.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.util.QueryDslUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	public void deleteByUserId(Long userId) {
		queryFactory.update(user)
			.set(user.deletedAt, LocalDateTime.now())
			.set(user.account, user.account.prepend("deleted_"))
			.where(user.id.eq(userId))
			.execute();
	}
}
