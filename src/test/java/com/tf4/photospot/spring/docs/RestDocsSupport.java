package com.tf4.photospot.spring.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.snippet.Attributes.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tf4.photospot.global.argument.CoordinateValidator;
import com.tf4.photospot.mockobject.RestDocsAuthenticationPrincipalArgumentResolver;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
	private static final String DOCUMENT_AUTO_FORMAT = "{class-name}/{method-name}";

	protected MockMvc mockMvc;
	protected ObjectMapper mapper;

	@BeforeEach
	void setUp(RestDocumentationContextProvider contextProvider) {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.apply(documentationConfiguration(contextProvider))
			.setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
			.setCustomArgumentResolvers(
				new PageableHandlerMethodArgumentResolver(),
				new RestDocsAuthenticationPrincipalArgumentResolver())
			.alwaysDo(MockMvcResultHandlers.print())
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.build();
	}

	protected abstract Object initController();

	protected RestDocumentationResultHandler restDocsTemplate(Snippet... snipets) {
		return document(DOCUMENT_AUTO_FORMAT,
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			snipets);
	}

	protected <T> Attribute defaultValue(T value) {
		return new Attribute("default", value);
	}

	protected Attribute constraints(String... message) {
		return new Attribute("constraints", message);
	}

	protected Attribute coordConstraints() {
		return constraints(
			CoordinateValidator.COORD_INVALID_RANGE,
			CoordinateValidator.COORD_NOT_EMPTY
		);
	}

	protected CustomResponseFieldsSnippet customResponseFields(
		String snippetFilePrefix,
		List<FieldDescriptor> fieldDescriptors
	) {
		return new CustomResponseFieldsSnippet(
			snippetFilePrefix,
			fieldDescriptors,
			null,
			true);
	}
}
