package com.tf4.photospot.album.presentation;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tf4.photospot.album.application.AlbumService;
import com.tf4.photospot.album.application.response.CreateAlbumPostResponse;
import com.tf4.photospot.album.presentation.request.PostIdListHttpRequest;
import com.tf4.photospot.mockobject.WithCustomMockUser;

@WithCustomMockUser
@WebMvcTest(controllers = AlbumController.class)
class AlbumControllerTest {
	@MockBean
	private AlbumService albumService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper mapper;

	@DisplayName("방명록 id 리스트는 양수만 가능하다")
	@Test
	void successRequestPositvePostIds() throws Exception {
		//given
		var response = List.of(
			new CreateAlbumPostResponse(1L, true),
			new CreateAlbumPostResponse(2L, false));
		given(albumService.addPosts(anyList(), anyLong(), anyLong())).willReturn(response);
		//when then
		mockMvc.perform(post("/api/v1/albums/{albumId}/posts", 1L).with(csrf())
				.content(mapper.writeValueAsString(new PostIdListHttpRequest(List.of(1L, 2L, 3L))))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isOk());
	}

	@DisplayName("방명록 id 리스트에 음수가 포함되면 Validation 예외가 발생한다")
	@Test
	void failRequestPositvePostIds() throws Exception {
		//given
		var response = List.of(
			new CreateAlbumPostResponse(1L, true),
			new CreateAlbumPostResponse(2L, false));
		given(albumService.addPosts(anyList(), anyLong(), anyLong())).willReturn(response);
		//when then
		mockMvc.perform(post("/api/v1/albums/{albumId}/posts", 1L).with(csrf())
				.content(mapper.writeValueAsString(new PostIdListHttpRequest(List.of(1L, 2L, -3L))))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isBadRequest());
	}
}
