package com.tf4.photospot.post.application.request;

import java.time.LocalDate;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.util.StringUtils;

import com.tf4.photospot.post.presentation.request.PostUploadHttpRequest;
import com.tf4.photospot.post.presentation.request.SpotInfoDto;

public record PostUploadRequest(
	Long userId,
	SpotInfoDto spotInfoDto,
	String photoUrl,
	Point photoCoord,
	LocalDate photoTakenAt,
	String detailAddress,
	List<Long> tags,
	List<Long> mentions,
	Boolean isPrivate
) {

	public PostUploadRequest {
		detailAddress = convertBlankToNull(detailAddress);
	}

	public static PostUploadRequest of(Long userId, PostUploadHttpRequest request) {
		return new PostUploadRequest(
			userId,
			request.spotInfo(),
			request.photoInfo().photoUrl(),
			request.photoInfo().coord().toCoord(),
			request.photoInfo().toDate(),
			convertBlankToNull(request.detailAddress()),
			request.tags(),
			request.mentions(),
			request.isPrivate()
		);
	}

	private static String convertBlankToNull(String str) {
		if (StringUtils.hasText(str)) {
			return str;
		}
		return null;
	}
}
