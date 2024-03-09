package com.tf4.photospot.bookmark.infrastructure;

import static com.tf4.photospot.bookmark.domain.QBookmark.*;
import static com.tf4.photospot.bookmark.domain.QBookmarkFolder.*;
import static com.tf4.photospot.spot.domain.QSpot.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tf4.photospot.bookmark.application.request.ReadBookmarkFolderList;
import com.tf4.photospot.bookmark.domain.Bookmark;
import com.tf4.photospot.bookmark.domain.BookmarkFolder;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.BookmarkErrorCode;
import com.tf4.photospot.global.util.QueryDslUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookmarkQueryRepository extends QueryDslUtils {
	private final JPAQueryFactory queryFactory;

	public Slice<Bookmark> findBookmarksOfFolder(Long bookmarkFolderId, Long userId, Pageable pageable) {
		var query = queryFactory.select(bookmark)
			.from(bookmark)
			.join(bookmark.spot, spot).fetchJoin()
			.join(bookmark.bookmarkFolder, bookmarkFolder)
			.where(bookmarkFolder.id.eq(bookmarkFolderId).and(bookmarkFolder.user.id.eq(userId)));
		return orderBy(query, bookmark, pageable).toSlice(query, pageable);
	}

	public List<BookmarkFolder> findBookmarkFolders(ReadBookmarkFolderList request) {
		return queryFactory.selectFrom(bookmarkFolder)
			.where(bookmarkFolder.user.id.eq(request.userId()))
			.orderBy(getBookmarkFolderOrder(request.direction()))
			.fetch();
	}

	private static OrderSpecifier<Long> getBookmarkFolderOrder(Sort.Direction direction) {
		return direction.isDescending() ? bookmarkFolder.id.desc() : bookmarkFolder.id.asc();
	}

	public Optional<BookmarkFolder> findBookmarkFolder(Long bookmarkFolderId) {
		return Optional.ofNullable(queryFactory.selectFrom(bookmarkFolder)
			.where(bookmarkFolder.id.eq(bookmarkFolderId))
			.fetchOne());
	}

	public int deleteBookmarks(BookmarkFolder bookmarkFolder, List<Long> bookmarkIds) {
		final long deleted = queryFactory.delete(bookmark)
			.where(bookmark.bookmarkFolder.eq(bookmarkFolder).and(bookmark.id.in(bookmarkIds)))
			.execute();
		if (bookmarkIds.size() != deleted) {
			throw new ApiException(BookmarkErrorCode.DELETED_BOOKMARKS_DO_NOT_MATCH);
		}
		return (int)deleted;
	}

	public void deleteBookmarkFolder(BookmarkFolder folder) {
		queryFactory.delete(bookmark)
			.where(bookmark.bookmarkFolder.eq(folder))
			.execute();
		queryFactory.delete(bookmarkFolder)
			.where(bookmarkFolder.eq(folder))
			.execute();
	}

	public boolean existsBookmark(Long bookmarkFolderId, Long spotId) {
		final Integer exists = queryFactory.selectOne()
			.from(bookmark)
			.where(bookmark.bookmarkFolder.id.eq(bookmarkFolderId).and(bookmark.spot.id.eq(spotId)))
			.fetchFirst();
		return exists != null;
	}
}
