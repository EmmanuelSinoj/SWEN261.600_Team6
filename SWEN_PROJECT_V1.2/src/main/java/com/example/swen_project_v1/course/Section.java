package com.example.swen_project_v1.course;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
public class Section {

    public static final int MAX_CAPACITY = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String crn;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "section_days", joinColumns = @JoinColumn(name = "section_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day")
    private List<DayOfWeek> days = new ArrayList<>();

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String room;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private String professor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMode deliveryMode = DeliveryMode.IN_PERSON;

    @Column(nullable = false)
    private int enrolledCount = 0;

    public Long getId() { return id; }
    public String getCrn() { return crn; }
    public Course getCourse() { return course; }
    public List<DayOfWeek> getDays() { return days; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getProfessor() { return professor; }
    public DeliveryMode getDeliveryMode() { return deliveryMode; }
    public int getEnrolledCount() { return enrolledCount; }

    public void setId(Long id) { this.id = id; }
    public void setCrn(String crn) { this.crn = crn; }
    public void setCourse(Course course) { this.course = course; }
    public void setDays(List<DayOfWeek> days) { this.days = days; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setRoom(String room) { this.room = room; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setProfessor(String professor) { this.professor = professor; }
    public void setDeliveryMode(DeliveryMode deliveryMode) { this.deliveryMode = deliveryMode; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }

    public boolean isFull() {
        return enrolledCount >= capacity;
    }

    public boolean hasTimeConflict(Section other) {
        // Check if any days overlap
        boolean daysOverlap = false;
        for (DayOfWeek day : this.days) {
            if (other.days.contains(day)) {
                daysOverlap = true;
                break;
            }
        }
        if (!daysOverlap) return false;

        // Check time overlap
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    public String getDaysString() {
        if (days == null || days.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < days.size(); i++) {
            if (i > 0) sb.append("/");
            sb.append(days.get(i).name());
        }
        return sb.toString();
    }
}