package com.tf4.photospot.post.application.response;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TagResponseTest {
	@DisplayName("태그 icon url이 없으면 \"\"로 생성")
	@Test
	void tagTest() {
		//given
		final TagResponse tagResponse = TagResponse.builder().tagId(1L).tagName("tag").build();
		//when //then
		assertThat(tagResponse.iconUrl()).isEqualTo("");
	}
}