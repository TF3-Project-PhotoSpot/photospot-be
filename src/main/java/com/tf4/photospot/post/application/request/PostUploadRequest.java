package com.tf4.photospot.post.application.request;

import java.time.LocalDate;
import java.util.List;

import org.locationtech.jts.geom.Point;

import com.tf4.photospot.post.presentation.request.PostUploadHttpRequest;
import com.tf4.photospot.post.presentation.request.SpotInfoDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostUploadRequest {

	private Long userId;
	private SpotInfoDto spotInfoDto;
	private String photoUrl;
	private Point photoCoord;
	private LocalDate photoTakenAt;
	private String detailAddress;
	private List<Long> tags;
	private List<Long> mentions;
	private Boolean isPrivate;

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
		if (str != null && str.isBlank()) {
			return null;
		}
		return str;
	}
}
