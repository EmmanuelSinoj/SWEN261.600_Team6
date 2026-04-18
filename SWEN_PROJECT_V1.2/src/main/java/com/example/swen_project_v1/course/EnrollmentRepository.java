package com.example.swen_project_v1.course;

import com.example.swen_project_v1.auth.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentAndSection(Student student, Section section);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.student = :student AND e.section.course = :course")
    boolean isAlreadyEnrolledInCourse(@Param("student") Student student, @Param("course") Course course);

    // --- NEW: needed for US-09B ---

    @Query("SELECT e FROM Enrollment e WHERE e.section.id = :sectionId AND e.status = :status ORDER BY e.enrollmentDate ASC")
    List<Enrollment> findBySectionIdAndStatusOrderByEnrollmentDateAsc(
            @Param("sectionId") Long sectionId,
            @Param("status") EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = :status")
    List<Enrollment> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.section.id = :sectionId AND e.status = :status")
    int countBySectionIdAndStatus(
            @Param("sectionId") Long sectionId,
            @Param("status") EnrollmentStatus status);
}