package com.example.swen_project_v1.cart;

import com.example.swen_project_v1.auth.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByStudent(Student student);
}