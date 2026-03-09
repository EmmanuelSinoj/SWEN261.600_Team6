package com.example.swen_project_v1.auth;

import jakarta.persistence.*;

@Entity
@Table(name="administrators", uniqueConstraints = @UniqueConstraint(columnNames = "staff_id"))
public class Administrator extends User {

    @Column(nullable=false, name="staff_id", unique=true)
    private String staffId;

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
}