package com.brs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BrsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrsBackendApplication.class, args);
    }
}
