package com.tf4.photospot.photo.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.photo.domain.Photo;
import com.tf4.photospot.photo.domain.PhotoRepository;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.support.IntegrationTestSupport;

public class PhotoServiceTest extends IntegrationTestSupport {

	@Autowired
	private PhotoService photoService;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private MockS3Config mockS3Config;

	@TestFactory
	@DisplayName("사진 업로드 시나리오")
	Collection<DynamicTest> uploadPhoto() {
		// given
		var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "<<jpeg data>>".getBytes());

		return List.of(
			DynamicTest.dynamicTest("사진 업로드에 성공한다", () -> {
				// when
				String photoUrl = photoService.upload(file).photoUrl();

				// then
				assertThat(photoUrl).isEqualTo(mockS3Config.getDummyUrl());
			}),
			DynamicTest.dynamicTest("유효하지 않은 확장자 파일을 받으면 예외를 던진다.", () -> {
				// given
				var gifFile = new MockMultipartFile("gif_file", "moving.gif", "image/gif", "<<gif data>>".getBytes());

				// when & then
				assertThatThrownBy(() -> photoService.upload(gifFile)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION.getMessage());
			}),
			DynamicTest.dynamicTest("비어있는 파일을 받으면 예외를 던진다.", () -> {
				// given
				var emptyFile = new MockMultipartFile("empty_file", "empty.jpeg", "image/jpeg", new byte[0]);

				// when & then
				assertThatThrownBy(() -> photoService.upload(emptyFile)).isInstanceOf(ApiException.class)
					.hasMessage(S3UploaderErrorCode.EMPTY_FILE.getMessage());
			})
		);
	}

	@TestFactory
	@DisplayName("사진 저장 시나리오")
	Collection<DynamicTest> savePhoto() {
		// given
		var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "<<jpg data>>".getBytes());
		String photoUrl = photoService.upload(file).photoUrl();
		Point coord = new GeometryFactory().createPoint(new Coordinate(23.0, 45.0));
		coord.setSRID(4326);
		LocalDate date = LocalDate.now().minusDays(1);

		return List.of(
			DynamicTest.dynamicTest("S3 temp 폴더에 저장된 사진을 post_images 폴더로 옮기고 사진 정보를 db에 저장한다.", () -> {
				// when
				Long photoId = photoService.save(photoUrl, coord, date).photoId();
				Photo savedPhoto = photoRepository.findById(photoId).get();

				// then
				assertAll(
					() -> assertThat(savedPhoto.getPhotoUrl()).doesNotContain(S3Directory.TEMP_FOLDER.getPath())
						.contains(S3Directory.POST_FOLDER.getPath()),
					() -> assertThat(savedPhoto.getPhotoUrl()).isEqualTo(mockS3Config.getDummyUrl()),
					() -> assertThat(savedPhoto.getTakenAt()).isEqualTo(date),
					() -> assertThat(savedPhoto.getCoord()).isEqualTo(coord)
				);
			})
		);
	}

}
