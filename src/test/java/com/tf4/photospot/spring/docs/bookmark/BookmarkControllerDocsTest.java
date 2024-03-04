package com.tf4.photospot.spring.docs.bookmark;

import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.bookmark.application.BookmarkService;
import com.tf4.photospot.bookmark.application.request.CreateBookmark;
import com.tf4.photospot.bookmark.application.request.CreateBookmarkFolder;
import com.tf4.photospot.bookmark.presentation.BookmarkController;
import com.tf4.photospot.bookmark.presentation.request.AddBookmarkHttpRequest;
import com.tf4.photospot.bookmark.presentation.request.CreateBookmarkFolderHttpRequest;
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
}
