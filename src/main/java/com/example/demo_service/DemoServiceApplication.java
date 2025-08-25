package com.example.demo_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DemoServiceApplication {

	public static void main(String[] args) {
		log.info("DemoServiceApplication :: main() :: Init");
		SpringApplication.run(DemoServiceApplication.class, args);
	}

}
