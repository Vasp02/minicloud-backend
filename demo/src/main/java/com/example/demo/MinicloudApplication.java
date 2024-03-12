package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.demo", "another.package.containing.JWTUtil"})
public class MinicloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinicloudApplication.class, args);
	}

}
