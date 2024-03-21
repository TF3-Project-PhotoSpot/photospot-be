package com.tf4.photospot.spring.docs.album;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.album.application.AlbumService;
import com.tf4.photospot.album.application.response.AlbumPreviewResponse;
import com.tf4.photospot.album.application.response.CreateAlbumPostResponse;
import com.tf4.photospot.album.presentation.AlbumController;
import com.tf4.photospot.album.presentation.request.CreateAlbumHttpRequest;
import com.tf4.photospot.album.presentation.request.PostIdListHttpRequest;
import com.tf4.photospot.global.dto.SlicePageDto;
import com.tf4.photospot.post.application.PostService;
import com.tf4.photospot.post.application.request.PostSearchCondition;
import com.tf4.photospot.post.application.response.BubbleResponse;
import com.tf4.photospot.post.application.response.PostDetailResponse;
import com.tf4.photospot.post.application.response.PostPreviewResponse;
import com.tf4.photospot.post.application.response.TagResponse;
import com.tf4.photospot.post.application.response.WriterResponse;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class AlbumControllerDocsTest extends RestDocsSupport {
	private final AlbumService albumService = mock(AlbumService.class);
	private final PostService postService = mock(PostService.class);

	@Override
	protected Object initController() {
		return new AlbumController(albumService, postService);
	}

	@Test
	void getPostPreviews() throws Exception {
		//given
		var response = SlicePageDto.wrap(List.of(
			new PostPreviewResponse(1L, 1L, "photoUrl")), false);
		given(albumService.getPostPreviewsOfAlbum(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/albums/{albumId}/posts/preview", 1L)
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("albumId").description("앨범 ID")),
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
	void getPosts() throws Exception {
		//given
		var response = SlicePageDto.wrap(List.of(PostDetailResponse.builder()
			.id(1L)
			.address("spot address")
			.detailAddress("detail address")
			.likeCount(10)
			.photoUrl("photoUrl")
			.bubble(new BubbleResponse("이미지 설명", 100, 200))
			.isLiked(true)
			.createdAt(LocalDateTime.of(2024, 1, 10, 12, 30))
			.writer(new WriterResponse(1L, true, "nickname", "profileUrl"))
			.tags(List.of(new TagResponse(1L, "iconUrl", "tagName")))
			.build()), false);
		given(albumService.getPostsOfAlbum(any(PostSearchCondition.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/albums/{albumId}/posts", 1L)
				.queryParam("page", "0")
				.queryParam("size", "10")
				.queryParam("sort", "id,desc")
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("albumId").description("앨범 ID")),
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
					fieldWithPath("content[].address").type(JsonFieldType.STRING).description("스팟 주소"),
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
					fieldWithPath("content[].writer.isWriter").type(JsonFieldType.BOOLEAN).description("작성자 본인 여부"),
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
	void createAlbum() throws Exception {
		//given
		given(albumService.create(anyLong(), anyString())).willReturn(1L);
		//when
		mockMvc.perform(post("/api/v1/albums")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new CreateAlbumHttpRequest("albumName")))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(fieldWithPath("name").type(JsonFieldType.STRING).description("앨범 이름")),
				responseFields(fieldWithPath("albumId").type(JsonFieldType.NUMBER).description("앨범 id"))
			));
	}

	@Test
	void addPosts() throws Exception {
		//given
		PostIdListHttpRequest request = new PostIdListHttpRequest(List.of(1L, 2L));
		final List<CreateAlbumPostResponse> responses = List.of(
			new CreateAlbumPostResponse(1L),
			new CreateAlbumPostResponse(2L)
		);
		given(albumService.addPosts(anyList(), anyLong(), anyLong())).willReturn(responses);
		//when
		mockMvc.perform(post("/api/v1/albums/{albumId}/posts", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("albumId").description("앨범 ID")),
				requestFields(fieldWithPath("postIds").type(JsonFieldType.ARRAY).description("방명록 id 리스트")),
				responseFields(
					fieldWithPath("failedPostIds").type(JsonFieldType.ARRAY).description("앨범에 추가못한 방명록 id 리스트")
				)
			));
	}

	@Test
	void removePosts() throws Exception {
		//given
		PostIdListHttpRequest request = new PostIdListHttpRequest(List.of(1L, 2L));
		willDoNothing().given(albumService).removePosts(anyList(), anyLong(), anyLong());
		//when
		mockMvc.perform(delete("/api/v1/albums/{albumId}/posts", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("albumId").description("앨범 ID")),
				requestFields(fieldWithPath("postIds").type(JsonFieldType.ARRAY).description("방명록 id 리스트")),
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공")))
			);
	}

	@Test
	void replacePosts() throws Exception {
		//given
		PostIdListHttpRequest request = new PostIdListHttpRequest(List.of(1L, 2L));
		willDoNothing().given(albumService).replacePosts(anyList(), anyLong(), anyLong());
		//when
		mockMvc.perform(delete("/api/v1/albums/{albumId}/posts", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("albumId").description("앨범 ID")),
				requestFields(fieldWithPath("postIds").type(JsonFieldType.ARRAY).description("방명록 id 리스트")),
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공"))
			));
	}

	@Test
	void removeAlbum() throws Exception {
		//given
		willDoNothing().given(albumService).remove(anyLong(), anyLong());
		//when
		mockMvc.perform(delete("/api/v1/albums/{albumId}", 1L))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				pathParameters(parameterWithName("albumId").description("앨범 ID")),
				responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("성공")))
			);
	}

	@Test
	void getAlbums() throws Exception {
		//given
		var postImageResponse = Optional.of("photoUrl");
		var albumsResponses = List.of(
			new AlbumPreviewResponse(1L, "album", "photoUrl"),
			new AlbumPreviewResponse(1L, "emptyAlbum", "")
		);
		given(postService.getFirstPostImage(any(PostSearchCondition.class))).willReturn(postImageResponse);
		given(albumService.getAlbums(anyLong())).willReturn(albumsResponses);
		//when
		mockMvc.perform(get("/api/v1/albums"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("myPost").type(JsonFieldType.STRING).description("내 방명록 미리보기 이미지 url")
						.attributes(defaultValue("\"\"")),
					fieldWithPath("likePost").type(JsonFieldType.STRING).description("좋아요한 방명록 미리보기 이미지 url")
						.attributes(defaultValue("\"\"")),
					fieldWithPath("albums").type(JsonFieldType.ARRAY).description("앨범 리스트"),
					fieldWithPath("albums[].albumId").type(JsonFieldType.NUMBER).description("앨범 id"),
					fieldWithPath("albums[].name").type(JsonFieldType.STRING).description("앨범 이름"),
					fieldWithPath("albums[].photoUrl").type(JsonFieldType.STRING).description("앨범 미리보기 이미지 url")
						.attributes(defaultValue("\"\""))
				)));
	}
}
