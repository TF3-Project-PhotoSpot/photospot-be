package com.tf4.photospot.bookmark.infrastructure;

import static com.tf4.photospot.bookmark.domain.QBookmark.*;
import static com.tf4.photospot.bookmark.domain.QBookmarkFolder.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.global.util.QueryDslUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookmarkQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	public Slice<Bookmark> findBookmarksOfFolder(Long bookmarkFolderId, Long userId, Pageable pageable) {
		var query = queryFactory.select(bookmark)
			.from(bookmark)
			.join(bookmark.bookmarkFolder, bookmarkFolder)
			.where(bookmarkFolder.id.eq(bookmarkFolderId).and(bookmarkFolder.user.id.eq(userId)));
		return orderBy(query, bookmark, pageable).toSlice(query, pageable);
	}
}
