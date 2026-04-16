package com.example.swen_project_v1.course;

import com.example.swen_project_v1.auth.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndSection(Student student, Section section);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.student = :student AND e.section.course = :course")
    boolean isAlreadyEnrolledInCourse(@Param("student") Student student, @Param("course") Course course);

}