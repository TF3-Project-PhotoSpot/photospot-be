package com.tf4.photospot.bookmark.infrastructure;

import static com.tf4.photospot.bookmark.domain.QBookmarkFolder.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.global.util.QueryDslUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookmarkQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	public boolean existsFolderName(Long userId, String folderName) {
		final Integer exists = queryFactory.selectOne()
			.from(bookmarkFolder)
			.where(bookmarkFolder.user.id.eq(userId).and(bookmarkFolder.name.eq(folderName)))
			.fetchFirst();
		return exists != null;
	}
}
