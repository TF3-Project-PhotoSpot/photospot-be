package com.tf4.photospot.spring.docs.post;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostListRequest;
import com.tf4.photospot.post.application.request.PostPreviewListRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.TagResponse;
import com.tf4.photospot.post.application.response.WriterResponse;
import com.tf4.photospot.post.presentation.PostController;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class PostControllerDocsTest extends RestDocsSupport {
	private final PostService postService = mock(PostService.class);

	@Override
	protected Object initController() {
		return new PostController(postService);
	}

	@Test
	void getPostPreviews() throws Exception {
		//given
		var response = SlicePageDto.wrap(List.of(
			new PostPreviewResponse(1L, 1L, "photoUrl")), false);
		given(postService.getPostPreviews(any(PostPreviewListRequest.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts/preview")
				.queryParam("spotId", "1")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
				.queryParam("sort", "likeCount,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("spotId").description("스팟 ID").attributes(coordConstraints()),
					parameterWithName("page").description("페이지").optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10)),
					parameterWithName("sort").description("정렬 옵션").optional()
						.attributes(constraints("id, likeCount"), defaultValue("id,desc"))
				),
				responseFields(
					fieldWithPath("content").type(JsonFieldType.ARRAY).description("방명록 상세 목록 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("content[].postId").type(JsonFieldType.NUMBER).description("방명록 ID"),
					fieldWithPath("content[].photoUrl").type(JsonFieldType.STRING).description("방명록 photo url"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 방명록 여부")
				)));
	}

	@Test
	void getPosts() throws Exception {
		//given
		var response = SlicePageDto.wrap(List.of(PostDetailResponse.builder()
			.id(1L)
			.detailAddress("detail address")
			.likeCount(10L)
			.photoUrl("photoUrl")
			.isLiked(true)
			.createdAt(LocalDateTime.of(2024, 1, 10, 12, 30))
			.writer(new WriterResponse(1L, "nickname", "profileUrl"))
			.tags(List.of(new TagResponse(1L, "iconUrl", "tagName")))
			.build()), false);
		given(postService.getPosts(any(PostListRequest.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts")
				.queryParam("spotId", "1")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
				.queryParam("sort", "likeCount,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("spotId").description("스팟 ID").attributes(coordConstraints()),
					parameterWithName("page").description("페이지").optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10)),
					parameterWithName("sort").description("정렬 옵션").optional()
						.attributes(constraints("id, likeCount"), defaultValue("id,desc"))
				),
				responseFields(
					fieldWithPath("content").type(JsonFieldType.ARRAY).description("방명록 상세 목록 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("content[].id").type(JsonFieldType.NUMBER).description("방명록 ID"),
					fieldWithPath("content[].detailAddress").type(JsonFieldType.STRING).description("방명록 상세 주소"),
					fieldWithPath("content[].likeCount").type(JsonFieldType.NUMBER).description("방명록 좋아요 개수"),
					fieldWithPath("content[].photoUrl").type(JsonFieldType.STRING).description("방명록 photo url"),
					fieldWithPath("content[].isLiked").type(JsonFieldType.BOOLEAN)
						.description("방명록 좋아요 여부"),
					fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("방명록 생성일")
						.description("생성"),
					fieldWithPath("content[].writer.id").type(JsonFieldType.NUMBER).description("작성자 ID"),
					fieldWithPath("content[].writer.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
					fieldWithPath("content[].writer.profileUrl").type(JsonFieldType.STRING)
						.description("작성자 profile url"),
					fieldWithPath("content[].tags").type(JsonFieldType.ARRAY).description("태그 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("content[].tags[].tagId").type(JsonFieldType.NUMBER).description("태그 ID"),
					fieldWithPath("content[].tags[].iconUrl").type(JsonFieldType.STRING).description("태그 icon url"),
					fieldWithPath("content[].tags[].tagName").type(JsonFieldType.STRING).description("태그 이름"),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 방명록 여부")
				)));
	}
}
