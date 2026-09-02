package com.prepgenius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class PrepGeniusApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrepGeniusApplication.class, args);
	}

}
