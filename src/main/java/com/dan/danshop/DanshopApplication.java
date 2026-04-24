package com.dan.danshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DanshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(DanshopApplication.class, args);
	}

}
