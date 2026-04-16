package com.example.swen_project_v1.auth;

import com.example.swen_project_v1.cart.Cart;
import com.example.swen_project_v1.course.Enrollment;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="students", uniqueConstraints = @UniqueConstraint(columnNames = "student_id"))
public class Student extends User {

    @Column(nullable=false, name="student_id", unique=true)
    private String studentId;

    @Column(nullable=false, name="current_credits")
    private int currentCredits = 0;

    @Column(nullable=false, name="max_credits")
    private int maxCredits = 18;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    // Add a getter
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public String getStudentId() { return studentId; }
    public int getCurrentCredits() { return currentCredits; }
    public int getMaxCredits() { return maxCredits; }

    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setCurrentCredits(int currentCredits) { this.currentCredits = currentCredits; }
    public void setMaxCredits(int maxCredits) { this.maxCredits = maxCredits; }
}