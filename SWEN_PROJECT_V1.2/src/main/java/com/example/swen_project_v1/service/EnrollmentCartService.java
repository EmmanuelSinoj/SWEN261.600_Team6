package com.example.swen_project_v1.service;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.cart.Cart;
import com.example.swen_project_v1.cart.CartRepository;
import com.example.swen_project_v1.course.Enrollment;
import com.example.swen_project_v1.course.EnrollmentRepository;
import com.example.swen_project_v1.course.EnrollmentStatus;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.course.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentCartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WaitlistProcessingService waitlistProcessingService;

    public EnrollmentCartService(CartRepository cartRepository,
                                 UserRepository userRepository,
                                 SectionRepository sectionRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 WaitlistProcessingService waitlistProcessingService) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.waitlistProcessingService = waitlistProcessingService;
    }

    @Transactional
    public void checkoutAllCartItems(String studentEmail) {
        User user = userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!(user instanceof Student student)) {
            throw new IllegalArgumentException("Only students can enroll.");
        }

        Cart cart = cartRepository.findByStudent(student)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found."));

        List<Section> sectionsInCart = cart.getSections();

        for (Section section : sectionsInCart) {
            if (section.getEnrolledCount() < section.getCapacity()) {
                section.setEnrolledCount(section.getEnrolledCount() + 1);
                sectionRepository.save(section);

                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setSection(section);
                enrollment.setStatus(EnrollmentStatus.ENROLLED);
                enrollmentRepository.save(enrollment);

                student.setCurrentCredits(student.getCurrentCredits() + section.getCourse().getCredits());
            } else {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(student);
                enrollment.setSection(section);
                enrollment.setStatus(EnrollmentStatus.WAITLISTED);
                enrollmentRepository.save(enrollment);
            }
        }

        cart.getSections().clear();
        cartRepository.save(cart);
        userRepository.save(student);
    }

    @Transactional
    public void dropEnrollment(String studentEmail, Long sectionId) {
        User user = userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!(user instanceof Student student)) {
            throw new IllegalArgumentException("Only students can drop courses.");
        }

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found."));

        Enrollment enrollment = enrollmentRepository.findByStudentAndSection(student, section)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new IllegalArgumentException("Only actively enrolled courses can be dropped.");
        }

        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        if (section.getEnrolledCount() > 0) {
            section.setEnrolledCount(section.getEnrolledCount() - 1);
            sectionRepository.save(section);
        }

        int updatedCredits = student.getCurrentCredits() - section.getCourse().getCredits();
        student.setCurrentCredits(Math.max(updatedCredits, 0));
        userRepository.save(student);

        waitlistProcessingService.processWaitlistForSection(sectionId);
    }
}