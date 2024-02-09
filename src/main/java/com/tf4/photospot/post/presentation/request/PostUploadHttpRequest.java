package com.tf4.photospot.post.presentation.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record PostUploadHttpRequest(

	@NotNull
	PhotoInfoDto photoInfo,

	@NotNull
	SpotInfoDto spotInfo,

	String detailAddress,

	List<Long> tags,

	List<Long> mentions,

	@NotNull
	Boolean isPrivate
) {
}
