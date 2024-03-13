package com.tf4.photospot.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class ThreadPoolConfig {

	@Bean
	public ThreadPoolTaskExecutor threadPoolTaskExecutorForSlack() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(5);
		executor.setCorePoolSize(5);
		executor.initialize();
		executor.setThreadNamePrefix("async-task-");
		return executor;
	}
}
