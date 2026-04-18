package com.example.swen_project_v1.service;

import org.springframework.stereotype.Service;

@Service
public class DropCourseService {

    public boolean dropCourse(Long sectionId, String studentEmail) {

        // TODO: replace with real logic later
        System.out.println("Dropping course: " + sectionId + " for " + studentEmail);

        // simulate success
        return true;
    }
}