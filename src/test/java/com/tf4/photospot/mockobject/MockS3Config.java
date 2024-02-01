package com.tf4.photospot.mockobject;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.S3UploaderErrorCode;

import io.awspring.cloud.s3.ObjectMetadata;
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
	private S3Client s3Client = Mockito.mock(S3Client.class);
	private static final String URL_PREFIX = "https://bucket.s3.ap-northeast-2.amazonaws.com/";

	@Bean
	@Primary
	public S3Template mockS3Template() {
		S3Template s3Template = Mockito.mock(S3Template.class);

		given(s3Template.upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
			.willAnswer(
				invocation -> {
					String key = invocation.getArgument(1);
					return createS3ResourceWithUrl(key);
				});

		given(s3Template.download(anyString(), anyString())).willAnswer(
			invocation -> {
				String key = invocation.getArgument(1);
				return createS3ResourceWithUrl(key);
			});

		return s3Template;
	}

	public S3Resource createS3ResourceWithUrl(String key) throws IOException {
		S3Resource s3Resource = Mockito.mock(S3Resource.class);
		given(s3Resource.getURL()).willAnswer(invocation -> {
			url = new URL(URL_PREFIX + key);
			return url;
		});
		return s3Resource;
	}

	@Bean
	@Primary
	public S3Client mockS3Client() {
		given(s3Client.copyObject(any(CopyObjectRequest.class))).willReturn(CopyObjectResponse.builder().build());
		given(s3Client.deleteObject(any(DeleteObjectRequest.class))).willReturn(DeleteObjectResponse.builder().build());
		return s3Client;
	}

	public String getDummyUrl() {
		return url.toString();
	}

	public void mockThrowExceptionOnCopy() {
		given(s3Client.copyObject(any(CopyObjectRequest.class))).willThrow(
			new ApiException(S3UploaderErrorCode.UNEXPECTED_COPY_FAIL));
	}

	public void mockThrowExceptionOnDelete() {
		given(s3Client.deleteObject(any(DeleteObjectRequest.class))).willThrow(
			new ApiException(S3UploaderErrorCode.UNEXPECTED_DELETE_FAIL));
	}

}
