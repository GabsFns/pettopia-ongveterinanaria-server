package com.example.OngVeterinaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class  OngVeterinariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OngVeterinariaApplication.class, args);
	}
}