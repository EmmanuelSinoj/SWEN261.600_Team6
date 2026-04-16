package com.example.swen_project_v1.course;

import com.example.swen_project_v1.auth.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "section_id")
    private Section section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    // Default constructor for JPA
    public Enrollment() {}

    // Parameterized constructor
    public Enrollment(Student student, Section section, EnrollmentStatus status) {
        this.student = student;
        this.section = section;
        this.status = status;
        this.enrollmentDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Section getSection() { return section; }
    public EnrollmentStatus getStatus() { return status; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }

    public void setStatus(EnrollmentStatus status) { this.status = status; }
}