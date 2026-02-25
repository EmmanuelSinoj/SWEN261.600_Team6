package com.example.swen_project_v1.auth;

import jakarta.persistence.*;

@Entity
@Table(name="students", uniqueConstraints = @UniqueConstraint(columnNames = "student_id"))
public class Student extends User {

    @Column(nullable=false, name="student_id", unique=true)
    private String studentId;

    @Column(nullable=false, name="current_credits")
    private int currentCredits = 0;

    @Column(nullable=false, name="max_credits")
    private int maxCredits = 18;

    public String getStudentId() { return studentId; }
    public int getCurrentCredits() { return currentCredits; }
    public int getMaxCredits() { return maxCredits; }

    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setCurrentCredits(int currentCredits) { this.currentCredits = currentCredits; }
    public void setMaxCredits(int maxCredits) { this.maxCredits = maxCredits; }
}