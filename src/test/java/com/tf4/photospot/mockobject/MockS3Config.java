package com.tf4.photospot.mockobject;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

@TestConfiguration
@Profile("test")
public class MockS3Config {

	private static final String DUMMY_URL = "https://example.com";

	@Bean
	@Primary
	public AmazonS3 mockAmazonS3Client() throws MalformedURLException {
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		given(amazonS3.putObject(any(PutObjectRequest.class))).willReturn(new PutObjectResult());
		given(amazonS3.getUrl(any(), any())).willReturn(new URL(DUMMY_URL));
		return amazonS3;
	}
}
