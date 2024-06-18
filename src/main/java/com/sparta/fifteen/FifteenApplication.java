package com.sparta.fifteen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sparta.fifteen.repository")
@EntityScan(basePackages = "com.sparta.fifteen.entity")
public class FifteenApplication {

    public static void main(String[] args) {
        SpringApplication.run(FifteenApplication.class, args);
    }

}
