package com.example.swen_project_v1.service;

import com.example.swen_project_v1.auth.Role;
import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.StudentRepository;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.course.Enrollment;
import com.example.swen_project_v1.course.EnrollmentStatus;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.course.SectionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SectionRepository sectionRepository;
    private final WaitlistProcessingService waitlistProcessingService;

    // Constructor Injection
    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          SectionRepository sectionRepository,
                          WaitlistProcessingService waitlistProcessingService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sectionRepository = sectionRepository;
        this.waitlistProcessingService = waitlistProcessingService;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
    }

    @Transactional
    public void createStudent(String firstName, String lastName, String studentId, String email, String password) {
        // Business Rule Checks
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("duplicate_email:A student with email '" + email + "' already exists.");
        }
        if (studentRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("duplicate_id:Student ID '" + studentId + "' is already taken.");
        }

        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setStudentId(studentId);
        student.setEmail(email);
        student.setPasswordHash(passwordEncoder.encode(password)); // BR-001 Validation
        student.setRole(Role.STUDENT);
        student.setActive(true);
        student.setCurrentCredits(0);
        student.setMaxCredits(18); // BR-007 Setup

        try {
            studentRepository.save(student);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("error:Unable to create student due to a database conflict.");
        }
    }

    @Transactional
    public void updateStudent(Long id, String firstName, String lastName, String studentId, String email, String password, boolean active) {
        Student student = getStudentById(id);

        if (!student.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("duplicate_email:Cannot update: Email '" + email + "' is already used.");
        }

        if (!student.getStudentId().equals(studentId) && studentRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("duplicate_id:Cannot update: Student ID '" + studentId + "' is already taken.");
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setStudentId(studentId);
        student.setEmail(email);
        student.setActive(active);

        if (password != null && !password.isBlank()) {
            student.setPasswordHash(passwordEncoder.encode(password));
        }

        studentRepository.save(student);
    }

    @Transactional
    public Student disableStudent(Long id) {
        Student student = getStudentById(id);
        student.setActive(false);
        return studentRepository.save(student);
    }

    @Transactional
    public Student deleteStudent(Long id) {
        Student student = getStudentById(id);

        // Collect the IDs of sections where this student was ENROLLED (seats being freed)
        List<Long> freedSectionIds = new ArrayList<>();

        for (Enrollment enrollment : student.getEnrollments()) {
            Section lockedSection = sectionRepository.findByIdWithPessimisticLock(enrollment.getSection().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Section not found."));

            if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
                lockedSection.setEnrolledCount(lockedSection.getEnrolledCount() - 1);
                freedSectionIds.add(lockedSection.getId()); // a seat just opened
            } else if (enrollment.getStatus() == EnrollmentStatus.WAITLISTED) {
                lockedSection.setWaitlistCount(lockedSection.getWaitlistCount() - 1);
            }

            sectionRepository.save(lockedSection);
        }

        // Delete the student FIRST (removes their enrollments from the waitlist)
        studentRepository.deleteById(id);

        // NOW process the waitlist for each freed seat
        for (Long sectionId : freedSectionIds) {
            waitlistProcessingService.processWaitlistForSection(sectionId);
        }

        return student;
    }
}