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
import com.tf4.photospot.post.application.request.PostUpdateRequest;
import com.tf4.photospot.post.application.response.BubbleResponse;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.PostSaveResponse;
import com.tf4.photospot.post.application.response.PostUpdateResponse;
import com.tf4.photospot.post.application.response.ReportResponse;
import com.tf4.photospot.post.application.response.TagResponse;
import com.tf4.photospot.post.application.response.WriterResponse;
import com.tf4.photospot.post.presentation.PostController;
import com.tf4.photospot.post.presentation.request.BubbleInfoDto;
import com.tf4.photospot.post.presentation.request.PhotoInfoDto;
import com.tf4.photospot.post.presentation.request.PostReportRequest;
import com.tf4.photospot.post.presentation.request.PostStateUpdateRequest;
import com.tf4.photospot.post.presentation.request.PostUpdateHttpRequest;
import com.tf4.photospot.post.presentation.request.PostUploadRequest;
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
			.bubble(new BubbleResponse("이미지 설명", 100, 200))
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
					fieldWithPath("content[].bubble").type(JsonFieldType.OBJECT).description("방명록 photo bubble 정보"),
					fieldWithPath("content[].bubble.text").type(JsonFieldType.STRING).description("bubble 내용"),
					fieldWithPath("content[].bubble.x").type(JsonFieldType.NUMBER).description("bubble 위치 x 좌표"),
					fieldWithPath("content[].bubble.y").type(JsonFieldType.NUMBER).description("bubble 위치 y 좌표"),
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
			.bubble(new BubbleResponse("이미지 설명", 100, 200))
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
					fieldWithPath("content[].bubble").type(JsonFieldType.OBJECT).description("방명록 photo bubble 정보"),
					fieldWithPath("content[].bubble.text").type(JsonFieldType.STRING).description("bubble 내용"),
					fieldWithPath("content[].bubble.x").type(JsonFieldType.NUMBER).description("bubble 위치 x 좌표"),
					fieldWithPath("content[].bubble.y").type(JsonFieldType.NUMBER).description("bubble 위치 y 좌표"),
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
			.bubble(new BubbleResponse("이미지 설명", 100, 200))
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
					fieldWithPath("content[].bubble").type(JsonFieldType.OBJECT).description("방명록 photo bubble 정보"),
					fieldWithPath("content[].bubble.text").type(JsonFieldType.STRING).description("bubble 내용"),
					fieldWithPath("content[].bubble.x").type(JsonFieldType.NUMBER).description("bubble 위치 x 좌표"),
					fieldWithPath("content[].bubble.y").type(JsonFieldType.NUMBER).description("bubble 위치 y 좌표"),
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
		var bubbleInfo = new BubbleInfoDto("이미지 설명", 100, 200);
		var spotInfo = new SpotInfoDto(spotCoord, "서울 마포구 동교동 158-26");
		var tags = List.of(1L, 2L, 3L);
		var mentions = List.of(4L, 5L, 6L);
		var httpRequest = new PostUploadRequest(photoInfo, bubbleInfo, spotInfo, "할리스", tags, mentions, false);
		var response = new PostSaveResponse(1L, 1L);

		given(postService.upload(anyLong(), any(PostUploadRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(httpRequest))
				.accept(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("photoInfo").type(JsonFieldType.OBJECT).description("사진 메타데이터"),
					fieldWithPath("photoInfo.photoUrl").type(JsonFieldType.STRING).description("사진 주소"),
					fieldWithPath("photoInfo.coord").type(JsonFieldType.OBJECT).description("사진 좌표"),
					fieldWithPath("photoInfo.coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("photoInfo.coord.lon").type(JsonFieldType.NUMBER).description("경도"),
					fieldWithPath("photoInfo.takenAt").description("사진이 찍힌 날짜 및 시간"),
					fieldWithPath("bubbleInfo").type(JsonFieldType.OBJECT)
						.description("사진 버블 메타데이터")
						.optional()
						.attributes(defaultValue("null")),
					fieldWithPath("bubbleInfo.text").type(JsonFieldType.STRING).description("버블 내용"),
					fieldWithPath("bubbleInfo.x").type(JsonFieldType.NUMBER).description("버블 위치 x 좌표"),
					fieldWithPath("bubbleInfo.y").type(JsonFieldType.NUMBER).description("버블 위치 y 좌표"),
					fieldWithPath("spotInfo").type(JsonFieldType.OBJECT).description("장소 메타데이터"),
					fieldWithPath("spotInfo.coord").type(JsonFieldType.OBJECT).description("장소 중심 좌표"),
					fieldWithPath("spotInfo.coord.lat").type(JsonFieldType.NUMBER).description("위도"),
					fieldWithPath("spotInfo.coord.lon").type(JsonFieldType.NUMBER).description("경도"),
					fieldWithPath("spotInfo.address").type(JsonFieldType.STRING).description("장소 중심 좌표를 주소로 변환한 값"),
					fieldWithPath("detailAddress").type(JsonFieldType.STRING)
						.description("작성자가 직접 입력한 상세 주소")
						.optional()
						.attributes(defaultValue("null")),
					fieldWithPath("tags").type(JsonFieldType.ARRAY)
						.description("작성자가 선택한 태그 id 리스트")
						.optional()
						.attributes(defaultValue("emptyList")),
					fieldWithPath("mentions").type(JsonFieldType.ARRAY)
						.description("작성자가 언급한 유저 id 리스트")
						.optional()
						.attributes(defaultValue("emptyList")),
					fieldWithPath("isPrivate").type(JsonFieldType.BOOLEAN)
						.description("사진 비공개 설정 유무")
						.optional()
						.attributes(defaultValue("false"))
				),
				responseFields(
					fieldWithPath("postId").type(JsonFieldType.NUMBER).description("업로드 된 방명록 id 반환"),
					fieldWithPath("spotId").type(JsonFieldType.NUMBER).description("업로드 된 방명록이 속한 스팟 id 반환")
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
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("postId").description("방명록 ID")),
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공")))
			);
	}

	@Test
	void cancelPostLike() throws Exception {
		//given
		willDoNothing().given(postService).cancelPostLike(anyLong(), anyLong());
		//when
		mockMvc.perform(delete("/api/v1/posts/{postId}/likes", 1L))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("postId").description("방명록 ID")),
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공")))
			);
	}

	@Test
	void updatePost() throws Exception {
		// given
		var tags = List.of(4L, 5L);
		var mentions = List.of(1L);
		var detailAd = "새로운 상세 주소";
		var httpRequest = new PostUpdateHttpRequest(tags, mentions, detailAd);
		var response = new PostUpdateResponse(1L, 1L);

		given(postService.update(any(PostUpdateRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(put("/api/v1/posts/{postId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(httpRequest))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("tags").type(JsonFieldType.ARRAY).description("작성자가 선택한 태그 id 리스트"),
					fieldWithPath("mentions").type(JsonFieldType.ARRAY).description("작성자가 언급한 유저 id 리스트"),
					fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("작성자가 직접 입력한 상세 주소")
				),
				responseFields(
					fieldWithPath("postId").type(JsonFieldType.NUMBER).description("수정된 방명록 id 반환"),
					fieldWithPath("spotId").type(JsonFieldType.NUMBER).description("수정된 방명록이 속한 스팟 id 반환")
				)
			));
	}

	@Test
	void updatePrivacyState() throws Exception {
		// given
		var httpRequest = new PostStateUpdateRequest(true);
		var response = new PostUpdateResponse(1L, 1L);
		given(postService.updatePrivacyState(anyLong(), anyLong(), anyBoolean())).willReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/posts/{postId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(httpRequest))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("isPrivate").type(JsonFieldType.BOOLEAN).description("방명록 공개 여부")
				),
				responseFields(
					fieldWithPath("postId").type(JsonFieldType.NUMBER).description("수정된 방명록 id 반환"),
					fieldWithPath("spotId").type(JsonFieldType.NUMBER).description("수정된 방명록이 속한 스팟 id 반환")
				)
			));
	}

	@Test
	void deletePost() throws Exception {
		// given
		willDoNothing().given(postService).delete(anyLong(), anyLong());

		// when & then
		mockMvc.perform(delete("/api/v1/posts/{postId}", 1L))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공")))
			);
	}

	@Test
	void reportPost() throws Exception {
		// given
		var request = new PostReportRequest("신고 사유");
		willDoNothing().given(postService).report(anyLong(), anyLong(), anyString());

		// when & then
		mockMvc.perform(post("/api/v1/posts/{postId}/report", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("message").type(JsonFieldType.STRING).description("성공")
				)
			));
	}

	@Test
	void getReports() throws Exception {
		// given
		var reports = List.of(
			new ReportResponse(1L, 1L, "작성자1", "방명록1 스팟 주소", "불쾌한 사진"),
			new ReportResponse(2L, 2L, "작성자2", "방명록2 스팟 주소", "징그러운 사진")
		);
		given(postService.getReports(anyLong())).willReturn(reports);

		// when & then
		mockMvc.perform(get("/api/v1/posts/reports"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("reports").type(JsonFieldType.ARRAY).description("사용자가 신고한 방명록 목록"),
					fieldWithPath("reports[].postId").type(JsonFieldType.NUMBER).description("신고한 방명록 아이디"),
					fieldWithPath("reports[].writerId").type(JsonFieldType.NUMBER).description("신고한 방명록 작성자 아이디"),
					fieldWithPath("reports[].writerNickname").type(JsonFieldType.STRING).description("신고한 방명록 작성자 닉네임"),
					fieldWithPath("reports[].spotAddress").type(JsonFieldType.STRING).description("신고한 방명록 스팟 주소"),
					fieldWithPath("reports[].reason").type(JsonFieldType.STRING).description("신고 이유")
				)
			));
	}

	@Test
	void getTags() throws Exception {
		// given
		final List<TagResponse> responses = List.of(
			TagResponse.builder().tagId(1L).tagName("tag1").iconUrl("iconUrl").build(),
			TagResponse.builder().tagId(2L).tagName("tag2").iconUrl("iconUrl").build()
		);
		given(postService.getTags()).willReturn(responses);
		// when & then
		mockMvc.perform(get("/api/v1/posts/tags"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("tags").type(JsonFieldType.ARRAY).description("태그 목록"),
					fieldWithPath("tags[].tagId").type(JsonFieldType.NUMBER).description("태그 id"),
					fieldWithPath("tags[].iconUrl").type(JsonFieldType.STRING).description("태그 icon url")
						.attributes(defaultValue("\"\"")),
					fieldWithPath("tags[].tagName").type(JsonFieldType.STRING).description("태그 이름"))
			));
	}
}
