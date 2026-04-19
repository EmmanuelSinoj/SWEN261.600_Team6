package com.example.swen_project_v1.web;

import com.example.swen_project_v1.course.TimetableBlockDTO;
import com.example.swen_project_v1.course.EnrollmentStatus;
import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.StudentRepository;
import com.example.swen_project_v1.course.Course;
import com.example.swen_project_v1.course.EnrolledSectionDTO;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.service.EnrollmentCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
public class EnrolledCoursesController {

    private final StudentRepository studentRepository;
    private final EnrollmentCartService enrollmentCartService;

    public EnrolledCoursesController(StudentRepository studentRepository,
                                     EnrollmentCartService enrollmentCartService) {
        this.studentRepository = studentRepository;
        this.enrollmentCartService = enrollmentCartService;
    }

    @GetMapping("/enrolled")
    public List<EnrolledSectionDTO> getEnrolledCourses(Authentication authentication) {

        String studentEmail = authentication.getName();
        Student student = studentRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return student.getEnrollments().stream()
                .map(enrollment -> {
                    Section section = enrollment.getSection();
                    Course course = section.getCourse();

                    String schedule = section.getDaysString() + " " +
                            section.getStartTime() + " - " + section.getEndTime();

                    return new EnrolledSectionDTO(
                            section.getId(),
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
    @DeleteMapping("/enrolled/{sectionId}/drop")
    public ResponseEntity<java.util.Map<String, String>> dropEnrolledCourse(
            @PathVariable Long sectionId,
            Authentication authentication) {

        try {
            enrollmentCartService.dropEnrollment(authentication.getName(), sectionId);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", "true",
                    "message", "Course dropped successfully."
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", "false",
                    "message", e.getMessage()
            ));
        }
    }
}