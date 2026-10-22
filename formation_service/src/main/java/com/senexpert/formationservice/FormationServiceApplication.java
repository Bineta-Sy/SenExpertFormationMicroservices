package com.senexpert.formationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FormationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormationServiceApplication.class, args);
	}

}
