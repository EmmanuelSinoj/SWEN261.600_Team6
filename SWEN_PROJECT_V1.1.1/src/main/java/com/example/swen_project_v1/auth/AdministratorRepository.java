package com.example.swen_project_v1.auth;

import com.example.swen_project_v1.auth.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByEmailIgnoreCase(String email);
}
