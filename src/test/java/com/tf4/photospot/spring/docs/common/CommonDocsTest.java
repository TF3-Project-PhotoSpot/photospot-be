package com.tf4.photospot.spring.docs.common;

import static com.tf4.photospot.spring.docs.common.CommonController.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import com.tf4.photospot.spring.docs.RestDocsSupport;

class CommonDocsTest extends RestDocsSupport {
	@Override
	protected Object initController() {
		return new CommonController();
	}

	@Test
	void commonSuccess() throws Exception {
		mockMvc.perform(get("/common/success"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드")
						.attributes(defaultValue("200")),
					fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
						.attributes(defaultValue("OK")),
					fieldWithPath("data").type(JsonFieldType.STRING).description("응답 데이터")
				)));
	}

	@Test
	void commonError() throws Exception {
		mockMvc.perform(get("/common/error"))
			.andExpect(status().isBadRequest())
			.andDo(restDocsTemplate(
				responseFields(
					fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
					fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
					fieldWithPath("errors").type(JsonFieldType.ARRAY).description("에러 데이터")
						.attributes(defaultValue("emptyList")),
					fieldWithPath("errors[].field").type(JsonFieldType.STRING).description("에러 필드")
						.attributes(defaultValue("\"\"")),
					fieldWithPath("errors[].value").type(JsonFieldType.STRING).description("에러 필드값")
						.attributes(defaultValue("\"\"")),
					fieldWithPath("errors[].message").type(JsonFieldType.STRING).description("에러 메시지")
				)));
	}

	@Test
	void commonErrorCodes() throws Exception {
		mockMvc.perform(get("/common/errorcodes"))
			.andExpect(status().isOk())
			.andDo(restDocsTemplate(
				customResponseFields("errorcode-response", createErrorCodeFieldDescriptors())
			));
	}

	private List<FieldDescriptor> createErrorCodeFieldDescriptors() {
		return getErrorCodeResponseMap().entrySet().stream()
			.map(entry -> {
				String name = entry.getKey();
				ErrorCodeResponse errorCodeResponse = entry.getValue();
				return fieldWithPath(name).type(JsonFieldType.OBJECT).attributes(
					key("type").value(errorCodeResponse.type()),
					key("code").value(errorCodeResponse.code()),
					key("status").value(errorCodeResponse.status()),
					key("message").value(errorCodeResponse.message())
				);
			}).toList();
	}
}
