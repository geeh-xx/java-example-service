package com.fedex.exampleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.NonNull;

@SpringBootApplication
public class IntegrationServiceApplication {

	public static void main(@NonNull final String[] args) {
		SpringApplication.run(IntegrationServiceApplication.class, args);
	}

}
