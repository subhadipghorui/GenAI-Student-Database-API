package com.example.genaistudentscore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.genaistudentscore.service.SqlGenerationService;

@SpringBootApplication
public class GenaistudentscoreApplication {

	@Autowired
	private SqlGenerationService sqlGenerationService;

	public static void main(String[] args) {
		SpringApplication.run(GenaistudentscoreApplication.class, args);

        System.out.println("Application started successfully! on port 8080");
	}

	@Bean
	public CommandLineRunner run() {
		return args -> {
			System.out.println("Fetching AI Model Info....");
//			String response = sqlGenerationService.fetchResponseFromGrok("Who are you?");
//			System.out.println("Prompt response: " + response);
		};
	}

}
