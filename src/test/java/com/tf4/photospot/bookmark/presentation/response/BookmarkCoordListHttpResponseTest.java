package com.tf4.photospot.bookmark.presentation.response;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class BookmarkCoordListHttpResponseTest {
	@Test
	void toGroupByFolderEmptyList() {
		List<BookmarkCoordFolderHttpResponse> responses = BookmarkCoordFolderHttpResponse.from(emptyList());
		assertThat(responses).isEmpty();
	}
}