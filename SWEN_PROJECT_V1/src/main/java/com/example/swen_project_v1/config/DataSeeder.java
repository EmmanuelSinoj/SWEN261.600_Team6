package com.example.swen_project_v1.config;

import com.example.swen_project_v1.auth.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmailIgnoreCase("admin@unienroll.com")) {
                Administrator admin = new Administrator();
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail("admin@unienroll.com");
                admin.setPasswordHash(encoder.encode("Admin123!"));
                admin.setRole(Role.ADMINISTRATOR);
                admin.setActive(true);
                admin.setStaffId("STAFF-0001");
                users.save(admin);
            }
        };
    }
}