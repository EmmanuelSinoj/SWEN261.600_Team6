package com.example.swen_project_v1.course;

import java.util.List;

public class TimetableBlockDTO {
    private String courseCode;
    private String title;
    private String professor;
    private String room;
    private String deliveryMode;
    private String status;
    private String startTime;
    private String endTime;
    private List<String> days;

    public TimetableBlockDTO(String courseCode, String title, String professor,
                             String room, String deliveryMode, String status,
                             String startTime, String endTime, List<String> days) {
        this.courseCode = courseCode;
        this.title = title;
        this.professor = professor;
        this.room = room;
        this.deliveryMode = deliveryMode;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
    }

    public String getCourseCode() { return courseCode; }
    public String getTitle() { return title; }
    public String getProfessor() { return professor; }
    public String getRoom() { return room; }
    public String getDeliveryMode() { return deliveryMode; }
    public String getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public List<String> getDays() { return days; }
}