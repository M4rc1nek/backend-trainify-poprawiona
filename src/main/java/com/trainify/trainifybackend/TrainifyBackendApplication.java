package com.trainify.trainifybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class TrainifyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainifyBackendApplication.class, args);
	}

}
