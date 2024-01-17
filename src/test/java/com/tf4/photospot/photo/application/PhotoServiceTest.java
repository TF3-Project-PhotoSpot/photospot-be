package com.tf4.photospot.photo.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.post.domain.Post;
import com.tf4.photospot.post.domain.PostRepository;

@Import(MockS3Config.class)
public class PhotoServiceTest extends IntegrationTestSupport {

	@Autowired
	private PhotoService photoService;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private PostRepository postRepository;

	private static Point coord;

	private LocalDate date;

	private static Long photoAId;

	private static Long photoBId;

	private static Photo photoWithPost;

	private static Photo photoWithoutPost;

	@BeforeEach
	void setup() {
		var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "<<jpg data>>".getBytes());
		coord = new GeometryFactory().createPoint(new Coordinate(23.0, 45.0));
		coord.setSRID(4326);
		date = LocalDate.now();

		photoAId = photoService.save(file, coord, date).photoId();
		photoBId = photoService.save(file, coord, date).photoId();
		photoWithPost = photoRepository.findById(photoAId).get();
		photoWithoutPost = photoRepository.findById(photoBId).get();
		var post = Post.builder().photo(photoWithPost).build();
		postRepository.save(post);
	}

	@TestFactory
	@DisplayName("사진 저장 시나리오")
	Collection<DynamicTest> savePhoto() {
		// given

		return List.of(
			DynamicTest.dynamicTest("사진 업로드에 성공한다.", () -> {
				// then
				assertAll(
					() -> assertThat(photoWithPost.getCoord()).isEqualTo(coord),
					() -> assertThat(photoWithPost.getTakenAt()).isEqualTo(date));
			}),
			DynamicTest.dynamicTest("유효하지 않은 확장자 파일을 받으면 예외를 던진다.", () -> {
				// given
				var gifFile = new MockMultipartFile("gif_file", "image.gif", "image/gif", "<gif data>>".getBytes());

				// when & then
				assertThatThrownBy(() -> photoService.save(gifFile, coord, date)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION.getMessage());
			}),
			DynamicTest.dynamicTest("비어있는 파일을 받으면 예외를 던진다.", () -> {
				//given
				var emptyFile = new MockMultipartFile("empty_file", "empty.jpeg", "image/jpeg", new byte[0]);

				// when & then
				assertThatThrownBy(() -> photoService.save(emptyFile, coord, date)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.EMPTY_FILE.getMessage());
			})
		);
	}

	@Test
	@DisplayName("일정 시간마다 유효한 이미지를 확인하고 이미지 url 폴더를 temp에서 post_images로 변경한다.")
	void scheduleToMove() throws InterruptedException {
		// given
		assertAll(
			() -> assertThat(photoWithPost.getPhotoUrl()).contains("temp"),
			() -> assertThat(photoWithoutPost.getPhotoUrl()).contains("temp")
		);

		// when
		photoService.movePhotos();
		var updatedSavedPhoto = photoRepository.findById(photoAId).get();
		var updatedPhotoWithoutPost = photoRepository.findById(photoBId).get();

		// then
		assertAll(
			() -> assertThat(updatedSavedPhoto.getPhotoUrl()).doesNotContain("temp")
				.contains("post_images"),
			() -> assertThat(updatedPhotoWithoutPost.getPhotoUrl()).doesNotContain("post_images")
				.contains("temp")
		);
	}

}
