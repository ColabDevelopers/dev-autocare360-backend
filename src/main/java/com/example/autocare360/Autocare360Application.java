package com.example.autocare360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Autocare360Application {

	public static void main(String[] args) {
		SpringApplication.run(Autocare360Application.class, args);
	}

}
