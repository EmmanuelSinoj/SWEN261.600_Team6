package com.example.swen_project_v1.course;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int minCredits = 1;

    @Column(nullable = false)
    private int maxCredits = 4;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();


    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    public int getMinCredits() { return minCredits; }
    public int getMaxCredits() { return maxCredits; }
    public String getDescription() { return description; }
    public List<Section> getSections() { return sections; }

    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setMinCredits(int minCredits) { this.minCredits = minCredits; }
    public void setMaxCredits(int maxCredits) { this.maxCredits = maxCredits; }
    public void setDescription(String description) { this.description = description; }
    public void setSections(List<Section> sections) { this.sections = sections; }
}