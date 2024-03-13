package com.tf4.photospot.user.infrastructure;

import static com.tf4.photospot.user.domain.QUser.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.UserErrorCode;
import com.tf4.photospot.global.util.QueryDslUtils;
import com.tf4.photospot.user.domain.User;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	private static final String DELETED_USER_ACCOUNT_PREFIX = "deleted_";

	public void deleteByUserId(Long userId) {
		final long deleted = queryFactory.update(user)
			.set(user.deletedAt, LocalDateTime.now())
			.set(user.account, user.account.prepend(DELETED_USER_ACCOUNT_PREFIX))
			.where(user.id.eq(userId).and(isActive()))
			.execute();
		if (deleted < 0) {
			throw new ApiException(UserErrorCode.NOT_FOUND_USER);
		}
	}

	public Optional<User> findActiveUserById(Long userId) {
		return Optional.ofNullable(queryFactory.selectFrom(user)
			.where(user.id.eq(userId).and(isActive()))
			.fetchOne());
	}

	private BooleanExpression isActive() {
		return user.deletedAt.isNull();
	}
}
