package com.tf4.photospot.spring.docs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
	protected MockMvc mockMvc;
	protected ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void setUp(RestDocumentationContextProvider contextProvider) {
		mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.apply(MockMvcRestDocumentation.documentationConfiguration(contextProvider))
			.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
			.build();
	}

	protected abstract Object initController();
}
