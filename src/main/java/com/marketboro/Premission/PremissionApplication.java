package com.marketboro.Premission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync

public class PremissionApplication {

	public static void main(String[] args) {
		SpringApplication.run(PremissionApplication.class, args);
	}

}
