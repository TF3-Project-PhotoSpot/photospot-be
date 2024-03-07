package com.tf4.photospot.spring.docs.bookmark;

import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.application.request.ReadBookmarkFolderList;
import com.tf4.photospot.bookmark.application.response.BookmarkFolderResponse;
import com.tf4.photospot.bookmark.application.response.BookmarkListResponse;
import com.tf4.photospot.bookmark.application.response.BookmarkResponse;
import com.tf4.photospot.bookmark.presentation.BookmarkController;
import com.tf4.photospot.bookmark.presentation.request.AddBookmarkHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.CreateBookmarkFolderHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.ReadBookmarkRequest;
import com.tf4.photospot.spring.docs.RestDocsSupport;

public class BookmarkControllerDocsTest extends RestDocsSupport {
	private final BookmarkService bookmarkService = mock(BookmarkService.class);

	@Override
	protected Object initController() {
		return new BookmarkController(bookmarkService);
	}

	@Test
	void createBookmarkFolder() throws Exception {
		//given
		var request = new CreateBookmarkFolderHttpRequest("name", "description", "color");
		given(bookmarkService.createFolder(any(CreateBookmarkFolder.class)))
			.willReturn(1L);
		//when
		mockMvc.perform(post("/api/v1/bookmarkFolders")
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("name").type(JsonFieldType.STRING).description("폴더 이름")
						.attributes(constraints("폴더 이름은 1글자 이상 10글자 이하입니다.")),
					fieldWithPath("description").type(JsonFieldType.STRING).description("폴더 설명")
						.attributes(constraints("폴더 설명은 30자 이하로 입력해주세요.")).optional(),
					fieldWithPath("color").type(JsonFieldType.STRING).description("폴더 색상")
						.attributes(constraints("색상은 필수입니다."))
				),
				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("북마크 폴더 id")
				)
			));
	}

	@Test
	void addBookmark() throws Exception {
		//given
		var request = new AddBookmarkHttpRequest(1L, "name", "description");
		given(bookmarkService.addBookmark(any(CreateBookmark.class))).willReturn(1L);
		//when
		mockMvc.perform(post("/api/v1/bookmarkFolders/{bookmarkFolderId}/bookmarks", 1L)
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				requestFields(
					fieldWithPath("spotId").type(JsonFieldType.NUMBER).description("스팟 ID")
						.attributes(constraints("양수만 입력 가능합니다.")),
					fieldWithPath("name").type(JsonFieldType.STRING).description("북마크 이름")
						.attributes(constraints("북마크 이름은 1글자 이상 10글자 이하입니다.")),
					fieldWithPath("description").type(JsonFieldType.STRING).description("북마크 설명").optional()
						.attributes(constraints("북마크 설명은 30자 이하로 입력해주세요."))
				),
				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("북마크 id")
				)
			));
	}

	@Test
	void getBookmarks() throws Exception {
		//given
		final BookmarkListResponse response = BookmarkListResponse.builder()
			.bookmarks(List.of(BookmarkResponse.builder()
				.id(1L)
				.spotId(1L)
				.name("북마크")
				.address("스팟 주소")
				.photoUrls(List.of("photoUrl1", "photoUrl2"))
				.build()))
			.hasNext(false)
			.build();
		given(bookmarkService.getBookmarks(any(ReadBookmarkRequest.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/bookmarkFolders/{bookmarkFolderId}/bookmarks", 1L)
				.queryParam("postPreviewCount", "5")
				.queryParam("page", "0")
				.queryParam("size", "10"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("postPreviewCount").description("미리보기 사진 개수")
						.optional().attributes(constraints("미리보기 사진은 1~10개만 가능합니다."), defaultValue(5)),
					parameterWithName("page").description("페이지")
						.optional().attributes(constraints("0부터 시작"), defaultValue(0)),
					parameterWithName("size").description("페이지당 개수")
						.optional().attributes(defaultValue(10))
				),
				responseFields(
					fieldWithPath("bookmarks").type(JsonFieldType.ARRAY).description("주변 추천 스팟 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("bookmarks[].id").type(JsonFieldType.NUMBER).description("북마크 ID"),
					fieldWithPath("bookmarks[].spotId").type(JsonFieldType.NUMBER).description("스팟 ID"),
					fieldWithPath("bookmarks[].name").type(JsonFieldType.STRING).description("북마크 이름"),
					fieldWithPath("bookmarks[].address").type(JsonFieldType.STRING).description("스팟 주소"),
					fieldWithPath("bookmarks[].photoUrls").type(JsonFieldType.ARRAY).description("최신 방명록 사진")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 여부")
				)));
	}

	@Test
	void getBookmarkFolders() throws Exception {
		//given
		final List<BookmarkFolderResponse> response = List.of(BookmarkFolderResponse.builder()
			.id(1L)
			.name("folderName")
			.description("description")
			.color("color")
			.bookmarkCount(5)
			.build());
		given(bookmarkService.getBookmarkFolders(any(ReadBookmarkFolderList.class))).willReturn(response);
		//when
		mockMvc.perform(get("/api/v1/bookmarkFolders")
				.queryParam("direction", "desc"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				queryParameters(
					parameterWithName("direction").description("정렬 방향").optional()
						.attributes(constraints("asc, desc"), defaultValue("desc"))),
				responseFields(
					fieldWithPath("bookmarkFolders").type(JsonFieldType.ARRAY).description("북마크 폴더 리스트")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("bookmarkFolders[].id").type(JsonFieldType.NUMBER).description("북마크 폴더 ID"),
					fieldWithPath("bookmarkFolders[].name").type(JsonFieldType.STRING).description("북마크 폴더 이름"),
					fieldWithPath("bookmarkFolders[].description").type(JsonFieldType.STRING)
						.description("북마크 폴더 설명").attributes(defaultValue("\"\"")),
					fieldWithPath("bookmarkFolders[].color").type(JsonFieldType.STRING).description("북마크 폴더 색상"),
					fieldWithPath("bookmarkFolders[].bookmarkCount").type(JsonFieldType.NUMBER)
						.description("현재 북마크 개수"),
					fieldWithPath("maxBookmarkCount").type(JsonFieldType.NUMBER).description("최대 북마크 개수")
				)));
	}
}
