package com.example.swen_project_v1.auth;

import com.example.swen_project_v1.auth.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    Optional<Student> findByEmailIgnoreCase(String email);
}