package com.timeless.bank_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BankSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankSystemApplication.class, args);
	}

}
