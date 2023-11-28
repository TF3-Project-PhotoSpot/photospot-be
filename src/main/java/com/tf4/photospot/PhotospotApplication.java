package com.tf4.photospot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PhotospotApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotospotApplication.class, args);
	}

}
