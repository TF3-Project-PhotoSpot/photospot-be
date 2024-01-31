package com.tf4.photospot.mockobject;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URL;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

@TestConfiguration
public class MockS3Config {

	private URL url;
	private String fileName;
	private static final String URL_PREFIX = "https://bucket_name.s3.ap-northeast-2.amazonaws.com/";

	@Bean
	@Primary
	public S3Template mockS3Template() throws IOException {
		fileName = "temp/example.webp";
		S3Template s3Template = Mockito.mock(S3Template.class);
		S3Resource s3Resource = Mockito.mock(S3Resource.class);

		given(s3Template.download(anyString(), anyString())).willReturn(s3Resource);
		given(s3Resource.getURL()).willAnswer(invocation -> {
			url = new URL(URL_PREFIX + fileName);
			return url;
		});
		return s3Template;
	}

	@Bean
	@Primary
	public S3Client mockS3Client() {
		S3Client s3Client = Mockito.mock(S3Client.class);

		given(s3Client.copyObject(any(CopyObjectRequest.class))).willAnswer(invocation -> {
			fileName = "post_images/example.webp";
			return CopyObjectResponse.builder().build();
		});
		given(s3Client.deleteObject(any(DeleteObjectRequest.class))).willReturn(DeleteObjectResponse.builder().build());
		return s3Client;
	}

	public String getDummyUrl() {
		return url.toString();
	}

}
