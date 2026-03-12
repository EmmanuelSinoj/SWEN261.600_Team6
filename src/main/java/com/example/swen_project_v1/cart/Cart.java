package com.example.swen_project_v1.cart;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.course.Section;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name = "cart_sections",
            joinColumns = @JoinColumn(name = "cart_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private List<Section> sections = new ArrayList<>();

    public Cart() {
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public int calculateTotalCredits() {
        int total = 0;
        for (Section section : sections) {
            total += section.getCourse().getCredits();
        }
        return total;
    }

    public boolean contains(Section section) {
        for (Section existing : sections) {
            if (existing.getId().equals(section.getId())) {
                return true;
            }
        }
        return false;
    }

    public void addSection(Section section) {
        if (!contains(section)) {
            sections.add(section);
            touch();
        }
    }

    public void removeSection(Section section) {
        sections.removeIf(existing -> existing.getId().equals(section.getId()));
        touch();
    }

    public void clear() {
        sections.clear();
        touch();
    }

    private void touch() {
        this.lastUpdated = LocalDateTime.now();
    }
}