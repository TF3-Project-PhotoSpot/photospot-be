package com.tf4.photospot.mockobject;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.amazonaws.services.s3.AmazonS3;

@Configuration
@Profile("test")
public class MockS3Config {
	@Bean
	@Primary
	public AmazonS3 mockAmazonS3Client() {
		return Mockito.mock(AmazonS3.class);
	}
}
