package com.tf4.photospot.post.presentation.request;

import java.util.List;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;

public record PostUploadRequest(

	@NotNull
	PhotoInfoDto photoInfo,

	BubbleInfoDto bubbleInfo,

	@NotNull
	SpotInfoDto spotInfo,

	String detailAddress,

	List<Long> tags,

	List<Long> mentions,

	@NotNull
	Boolean isPrivate
) {
	@JsonIgnore
	public String getValidAddress() {
		if (StringUtils.hasText(detailAddress)) {
			return detailAddress;
		}
		return null;
	}
}
