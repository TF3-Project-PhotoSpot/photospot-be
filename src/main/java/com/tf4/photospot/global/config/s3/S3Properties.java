package com.tf4.photospot.global.config.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cloud.aws.credentials")
public class S3Properties {

	private String accessKey;
	private String secretKey;
}
