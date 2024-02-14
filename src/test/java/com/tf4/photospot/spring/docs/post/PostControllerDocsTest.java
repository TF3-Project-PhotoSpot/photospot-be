package com.tf4.photospot.spring.docs.post;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.global.dto.CoordinateDto;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.request.PostUploadRequest;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostUploadResponse;
import com.tf4.photospot.post.application.response.TagResponse;
import com.tf4.photospot.post.application.response.WriterResponse;
import com.tf4.photospot.post.presentation.PostController;
import com.tf4.photospot.post.presentation.request.PhotoInfoDto;
import com.tf4.photospot.post.presentation.request.PostUploadHttpRequest;
import com.tf4.photospot.post.presentation.request.SpotInfoDto;
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
		given(postService.getPostPreviews(any(PostSearchCondition.class))).willReturn(response);
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
		given(postService.getPosts(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts")
				.queryParam("spotId", "1")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
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

	@Test
	void getMyPostPreviews() throws Exception {
		//given
		var response = SlicePageDto.wrap(List.of(
			new PostPreviewResponse(1L, 1L, "photoUrl")), false);
		given(postService.getPostPreviews(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts/mine/preview")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("page").description("페이지").optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10)),
					parameterWithName("sort").description("정렬 옵션").optional()
						.attributes(constraints("id"), defaultValue("id,desc"))
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
	void getMyPosts() throws Exception {
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
		given(postService.getPosts(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts/mine")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("page").description("페이지").optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10)),
					parameterWithName("sort").description("정렬 옵션").optional()
						.attributes(constraints("id"), defaultValue("id,desc"))
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

	@Test
	void getLikePostPreviews() throws Exception {
		//given
		var response = SlicePageDto.wrap(List.of(
			new PostPreviewResponse(1L, 1L, "photoUrl")), false);
		given(postService.getPostPreviews(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts/likes/preview")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("page").description("페이지").optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10)),
					parameterWithName("sort").description("정렬 옵션").optional()
						.attributes(constraints("id"), defaultValue("id,desc"))
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
	void getLikePosts() throws Exception {
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
		given(postService.getPosts(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/posts/likes")
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("page").description("페이지").optional()
						.attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수").optional().attributes(defaultValue(10)),
					parameterWithName("sort").description("정렬 옵션").optional()
						.attributes(constraints("id"), defaultValue("id,desc"))
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

	@Test
	void uploadPost() throws Exception {
		// given
		var photoCoord = new CoordinateDto(35.512, 126.912);
		var spotCoord = new CoordinateDto(35.557, 126.923);
		var photoInfo = new PhotoInfoDto("https://bucket.s3.ap-northeast-2.amazonaws.com/temp/example.webp", photoCoord,
			"2024-01-13T05:20:18.981+09:00");
		var spotInfo = new SpotInfoDto(spotCoord, "서울 마포구 동교동 158-26");
		var tags = List.of(1L, 2L, 3L);
		var mentions = List.of(4L, 5L, 6L);
		var httpRequest = new PostUploadHttpRequest(photoInfo, spotInfo, "할리스", tags, mentions, false);
		var response = new PostUploadResponse(1L);

		given(postService.upload(any(PostUploadRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(httpRequest))
				.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("photoInfo").description("사진 정보"),
					fieldWithPath("photoInfo.photoUrl").description("사진 주소"),
					fieldWithPath("photoInfo.coord").description("사진 좌표"),
					fieldWithPath("photoInfo.coord.lat").description("위도"),
					fieldWithPath("photoInfo.coord.lon").description("경도"),
					fieldWithPath("photoInfo.takenAt").description("사진이 찍힌 날짜 및 시간"),
					fieldWithPath("spotInfo").description("장소 정보"),
					fieldWithPath("spotInfo.coord").description("장소 중심 좌표"),
					fieldWithPath("spotInfo.coord.lat").description("위도"),
					fieldWithPath("spotInfo.coord.lon").description("경도"),
					fieldWithPath("spotInfo.address").description("장소 중심 주소"),
					fieldWithPath("detailAddress").description("작성자가 직접 입력한 상세 주소"),
					fieldWithPath("tags").description("작성자가 선택한 태그 아이디 리스트"),
					fieldWithPath("mentions").description("작성자가 언급한 유저 아이디 리스트"),
					fieldWithPath("isPrivate").description("사진 비공개 설정 유무").optional().attributes(defaultValue(false))
				),
				responseFields(
					fieldWithPath("postId").type(JsonFieldType.NUMBER).description("업로드 된 방명록 id 반환")
				)
			));
	}

	@Test
	void likePost() throws Exception {
		//given
		willDoNothing().given(postService).likePost(anyLong(), anyLong());
		//when
		mockMvc.perform(post("/api/v1/posts/{postId}/likes", 1L))
			.andExpect(status().isOk())
			.andDo(restDocsTemplateDefaultSuccess());
	}

	@Test
	void cancelPostLike() throws Exception {
		//given
		willDoNothing().given(postService).cancelPostLike(anyLong(), anyLong());
		//when
		mockMvc.perform(delete("/api/v1/posts/{postId}/likes", 1L))
			.andExpect(status().isOk())
			.andDo(restDocsTemplateDefaultSuccess());
	}
}
