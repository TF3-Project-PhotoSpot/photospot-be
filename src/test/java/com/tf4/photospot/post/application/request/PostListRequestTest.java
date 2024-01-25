package com.tf4.photospot.post.application.request;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.tf4.photospot.global.exception.domain.CommonErrorCode;

class PostListRequestTest {
	@DisplayName("정렬 가능한 property로 페이징 요청을 생성할 수 있다.")
	@Test
	void successPostListRequest() {
		//given
		final List<Sort> sortableList = List.of(
			Sort.by(Sort.Order.desc("id")),
			Sort.by(Sort.Order.desc("likeCount")),
			Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("id"))
		);
		//when then
		assertThatStream(sortableList.stream()).allSatisfy(
			sort -> new PostListRequest(1L, 1L, PageRequest.of(0, 10, sort)));
	}

	@DisplayName("정렬 불가능한 property로 페이징 요청을 생성하면 CANNOT_SORTED_PROPERTY 예외가 발생한다.")
	@Test
	void failCreatePostListRequest() {
		//given
		final Sort unsortable = Sort.by(Sort.Order.desc("detailAddress"));
		assertThatThrownBy(() -> new PostListRequest(1L, 1L, PageRequest.of(0, 10, unsortable)))
			.extracting("errorCode")
			.isEqualTo(CommonErrorCode.CANNOT_SORTED_PROPERTY);
	}

}