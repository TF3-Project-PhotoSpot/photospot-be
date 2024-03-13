package com.tf4.photospot.post.application.request;

import java.util.Optional;

import org.springframework.util.StringUtils;

import com.tf4.photospot.photo.domain.Bubble;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.presentation.request.PostUploadRequest;
import com.tf4.photospot.spot.domain.Spot;
import com.tf4.photospot.user.domain.User;

public record PostCreateDto(
	Spot spot,
	User writer,
	Photo photo,
	String detailAddress,
	boolean isPrivate
) {
	private static final String EMPTY_CONTENT = "";

	public static PostCreateDto of(Spot spot, User writer, PostUploadRequest request, String postPhotoUrl) {
		Photo.PhotoBuilder photoBuilder = Photo.builder()
			.photoUrl(postPhotoUrl)
			.coord(request.photoInfo().coord().toCoord())
			.takenAt(request.photoInfo().toDate());
		Optional.ofNullable(request.bubbleInfo())
			.ifPresent(info -> photoBuilder.bubble(
				Bubble.builder()
					.text(info.text())
					.posX(info.x())
					.posY(info.y())
					.build()
			));
		final Photo photo = photoBuilder.build();
		return new PostCreateDto(spot, writer, photo, request.detailAddress(), request.isPrivate());
	}

	public Post toPost() {
		return Post.builder()
			.spot(spot)
			.writer(writer)
			.photo(photo)
			.detailAddress(getValidAddress())
			.isPrivate(isPrivate)
			.build();
	}

	public String getValidAddress() {
		if (StringUtils.hasText(detailAddress)) {
			return detailAddress;
		}
		return EMPTY_CONTENT;
	}
}
