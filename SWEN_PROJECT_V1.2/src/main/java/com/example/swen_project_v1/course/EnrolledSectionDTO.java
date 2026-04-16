package com.example.swen_project_v1.course;

public class EnrolledSectionDTO {
    private String courseCode;
    private String title;
    private String professor;
    private String schedule;

    private int credits;
    private String status;
    private String room;
    private String deliveryMode;

    public EnrolledSectionDTO(String courseCode, String title, String professor, String schedule, int credits, String status, String room, String deliveryMode) {
        this.courseCode = courseCode;
        this.title = title;
        this.professor = professor;
        this.schedule = schedule;
        this.credits = credits;
        this.status = status;
        this.room = room;
        this.deliveryMode = deliveryMode;
    }

    // Getters are required for Spring Boot to convert this to JSON
    public String getCourseCode() { return courseCode; }
    public String getTitle() { return title; }
    public String getProfessor() { return professor; }
    public String getSchedule() { return schedule; }
    public int getCredits() { return credits; }
    public String getStatus() { return status; }
    public String getRoom() { return room; }
    public String getDeliveryMode() { return deliveryMode; }
}

