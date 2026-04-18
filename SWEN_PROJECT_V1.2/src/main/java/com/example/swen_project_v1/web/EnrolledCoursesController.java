package com.example.swen_project_v1.web;

import com.example.swen_project_v1.course.TimetableBlockDTO;
import com.example.swen_project_v1.course.EnrollmentStatus;
import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.StudentRepository;
import com.example.swen_project_v1.course.Course;
import com.example.swen_project_v1.course.EnrolledSectionDTO;
import com.example.swen_project_v1.course.Section;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class EnrolledCoursesController {

    private final StudentRepository studentRepository;

    public EnrolledCoursesController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/enrolled")
    public List<EnrolledSectionDTO> getEnrolledCourses(Authentication authentication) {

        String studentEmail = authentication.getName();
        Student student = studentRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Filter for active enrollments and map to the DTO
        return student.getEnrollments().stream()
                //.filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .map(enrollment -> {
                    Section section = enrollment.getSection();
                    Course course = section.getCourse();

                    // Combine days and times for the schedule string
                    String schedule = section.getDaysString() + " " +
                            section.getStartTime() + " - " + section.getEndTime();

                    return new EnrolledSectionDTO(
                            course.getCode(),
                            course.getTitle(),
                            section.getProfessor(),
                            schedule,
                            course.getCredits(),
                            enrollment.getStatus().name(),
                            section.getRoom(),
                            section.getDeliveryMode().name()
                    );

                })
                .collect(Collectors.toList());

    }
    @GetMapping("/me")
    public java.util.Map<String, String> getStudentInfo(Authentication authentication) {
        String studentEmail = authentication.getName();

        Student student = studentRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return java.util.Map.of(
                "firstName", student.getFirstName(),
                "lastName", student.getLastName()
        );
    }


    @GetMapping("/timetable")
    public List<TimetableBlockDTO> getTimetable(Authentication authentication) {
        String studentEmail = authentication.getName();

        Student student = studentRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return student.getEnrollments().stream()
                .filter(enrollment ->
                        enrollment.getStatus() == EnrollmentStatus.ENROLLED ||
                                enrollment.getStatus() == EnrollmentStatus.WAITLISTED)
                .map(enrollment -> {
                    Section section = enrollment.getSection();
                    Course course = section.getCourse();

                    List<String> days = section.getDays().stream()
                            .map(Enum::name)
                            .toList();

                    return new TimetableBlockDTO(
                            course.getCode(),
                            course.getTitle(),
                            section.getProfessor(),
                            section.getRoom(),
                            section.getDeliveryMode().name(),
                            enrollment.getStatus().name(),
                            section.getStartTime().toString(),
                            section.getEndTime().toString(),
                            days
                    );
                })
                .toList();
    }

}