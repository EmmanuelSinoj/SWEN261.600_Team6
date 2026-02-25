package com.example.swen_project_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.swen_project_v1")
public class SwenProjectV1Application {
    public static void main(String[] args) {
        SpringApplication.run(SwenProjectV1Application.class, args);
    }
}
