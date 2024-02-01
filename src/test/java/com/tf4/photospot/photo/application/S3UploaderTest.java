package com.tf4.photospot.photo.application;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import com.tf4.photospot.mockobject.MockS3Config;
import com.tf4.photospot.photo.domain.S3Directory;
import com.tf4.photospot.support.IntegrationTestSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Import(MockS3Config.class)
public class S3UploaderTest extends IntegrationTestSupport {

	private final S3Uploader s3Uploader;

	private final MockS3Config mockS3Config;

	@TestFactory
	@DisplayName("요청 유형에 따른 사진 업로드 시나리오")
	Collection<DynamicTest> uploadDependsOnRequest() {
		// given
		var file = new MockMultipartFile("file", "example.webp", "image/webp", "<<webp data>>".getBytes());

		return List.of(
			DynamicTest.dynamicTest("방명록 사진을 업로드하는 경우 temp 폴더에 저장한다.", () -> {
				// given
				String folder = S3Directory.TEMP_FOLDER.getFolder();

				// when
				String imageUrl = s3Uploader.upload(file, folder);

				// then
				assertThat(imageUrl).contains(S3Directory.TEMP_FOLDER.getPath())
					.doesNotContain(S3Directory.PROFILE_FOLDER.getPath())
					.doesNotContain(S3Directory.POST_FOLDER.getPath());
			}),
			DynamicTest.dynamicTest("프로필 사진을 업로드하는 경우 profile 폴더에 저장한다.", () -> {
				//given
				String folder = S3Directory.PROFILE_FOLDER.getFolder();

				// when
				String imageUrl = s3Uploader.upload(file, folder);

				// then
				assertThat(imageUrl).contains(S3Directory.PROFILE_FOLDER.getPath())
					.doesNotContain(S3Directory.TEMP_FOLDER.getPath())
					.doesNotContain(S3Directory.POST_FOLDER.getPath());
			})
		);
	}

}
