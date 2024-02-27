package com.tf4.photospot.global.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
	private String host;
	private int port;
	private String password;
}
