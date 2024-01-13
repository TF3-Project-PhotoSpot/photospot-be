package com.tf4.photospot.photo.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.tf4.photospot.IntegrationTestSupport;
import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;
import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.photo.domain.PhotoRepository;

@Import(MockS3Config.class)
public class PhotoServiceTest extends IntegrationTestSupport {

	private static final String DUMMY_URL = "https://example.com";

	@Autowired
	private PhotoService photoService;

	@Autowired
	private PhotoRepository photoRepository;

	@Autowired
	private AmazonS3 amazonS3;

	@BeforeEach
	void setup() throws MalformedURLException {
		given(amazonS3.putObject(any(PutObjectRequest.class))).willReturn(new PutObjectResult());
		given(amazonS3.getUrl(any(), any())).willReturn(new URL(DUMMY_URL));
	}

	@TestFactory
	@DisplayName("사진 업로드 시나리오")
	Collection<DynamicTest> savePhoto() {
		// given
		var file = new MockMultipartFile("file", "test.jpeg", "image/jpeg", "<<jpg data>>".getBytes());
		var coord = new GeometryFactory().createPoint(new Coordinate(23.0, 45.0));
		coord.setSRID(4326);
		LocalDate date = LocalDate.now();

		return List.of(DynamicTest.dynamicTest("사진 업로드에 성공한다.", () -> {
			// when
			var photoId = photoService.save(file, coord, date).photoId();
			var savedPhoto = photoRepository.findById(photoId).get();

			// then
			assertAll(() -> assertThat(savedPhoto.getPhotoUrl()).isEqualTo(DUMMY_URL),
				() -> assertThat(savedPhoto.getCoord()).isEqualTo(coord),
				() -> assertThat(savedPhoto.getTakenAt()).isEqualTo(date));
		}), DynamicTest.dynamicTest("유효하지 않은 확장자 파일을 받으면 예외를 던진다.", () -> {
			// given
			var gifFile = new MockMultipartFile("gif_file", "image.gif", "image/gif", "<gif data>>".getBytes());

			// when & then
			assertThatThrownBy(() -> photoService.save(gifFile, coord, date)).isInstanceOf(ApiException.class)
				.hasMessage(S3UploaderErrorCode.INVALID_PHOTO_EXTENSION.getMessage());
		}));
	}

}
