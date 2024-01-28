package com.tf4.photospot.global.util;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

public abstract class PageUtils {
	public static <T> Slice<T> toSlice(Pageable pageable, List<T> contents) {
		if (contents.size() > pageable.getPageSize()) {
			return new SliceImpl<>(contents.subList(0, pageable.getPageSize()), pageable, true);
		}
		return new SliceImpl<>(contents, pageable, false);
	}
}
