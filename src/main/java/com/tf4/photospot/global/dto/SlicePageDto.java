package com.tf4.photospot.global.dto;

import java.util.List;

import org.springframework.data.domain.Slice;

public record SlicePageDto<T>(
	List<T> content,
	Boolean hasNext
) {
	public static <T> SlicePageDto<T> wrap(Slice<T> slice) {
		return new SlicePageDto<>(slice.getContent(), slice.hasNext());
	}

	public static <T> SlicePageDto<T> wrap(List<T> content, Boolean hasNext) {
		return new SlicePageDto<>(content, hasNext);
	}
}
