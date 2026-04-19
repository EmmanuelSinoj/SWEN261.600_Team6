package com.example.swen_project_v1;

import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.course.Enrollment;
import com.example.swen_project_v1.course.EnrollmentRepository;
import com.example.swen_project_v1.course.EnrollmentStatus;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.course.SectionRepository;
import com.example.swen_project_v1.service.WaitlistProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class US09BWaitlistProcessingTest {

    @Autowired
    private WaitlistProcessingService waitlistProcessingService;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void waitlistedStudent_getsEnrolled_whenSeatBecomesAvailable() {
        Section section = sectionRepository.findAll().stream()
                .filter(s -> s.getWaitlistCount() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No section with waitlisted students found."));

        List<Enrollment> waitlist = enrollmentRepository
                .findBySectionIdAndStatusOrderByEnrollmentDateAsc(section.getId(), EnrollmentStatus.WAITLISTED);

        assertFalse(waitlist.isEmpty(), "Expected at least one waitlisted student.");

        Enrollment candidate = waitlist.get(0);
        int oldEnrolled = section.getEnrolledCount();
        int oldWaitlist = section.getWaitlistCount();

        if (section.getEnrolledCount() >= section.getCapacity()) {
            section.setCapacity(section.getEnrolledCount() + 1);
            sectionRepository.saveAndFlush(section);
        }

        waitlistProcessingService.processWaitlistForSection(section.getId());

        Enrollment updatedCandidate = enrollmentRepository.findById(candidate.getId())
                .orElseThrow(() -> new IllegalStateException("Enrollment not found after processing."));

        Section updatedSection = sectionRepository.findById(section.getId())
                .orElseThrow(() -> new IllegalStateException("Section not found after processing."));

        assertEquals(EnrollmentStatus.ENROLLED, updatedCandidate.getStatus());
        assertEquals(oldEnrolled + 1, updatedSection.getEnrolledCount());
        assertEquals(oldWaitlist - 1, updatedSection.getWaitlistCount());
    }

    @Test
    void waitlistedStudent_staysWaitlisted_whenInvalid() {
        Section section = sectionRepository.findAll().stream()
                .filter(s -> s.getWaitlistCount() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No section with waitlisted students found."));

        List<Enrollment> waitlist = enrollmentRepository
                .findBySectionIdAndStatusOrderByEnrollmentDateAsc(section.getId(), EnrollmentStatus.WAITLISTED);

        assertFalse(waitlist.isEmpty(), "Expected at least one waitlisted student.");

        Enrollment candidate = waitlist.get(0);

        candidate.getStudent().setCurrentCredits(candidate.getStudent().getMaxCredits() + 1);
        userRepository.save(candidate.getStudent());

        int oldEnrolled = section.getEnrolledCount();
        int oldWaitlist = section.getWaitlistCount();

        if (section.getEnrolledCount() >= section.getCapacity()) {
            section.setCapacity(section.getEnrolledCount() + 1);
            sectionRepository.saveAndFlush(section);
        }

        waitlistProcessingService.processWaitlistForSection(section.getId());

        Enrollment updatedCandidate = enrollmentRepository.findById(candidate.getId())
                .orElseThrow(() -> new IllegalStateException("Enrollment not found after processing."));

        Section updatedSection = sectionRepository.findById(section.getId())
                .orElseThrow(() -> new IllegalStateException("Section not found after processing."));

        assertEquals(EnrollmentStatus.WAITLISTED, updatedCandidate.getStatus());
        assertEquals(oldEnrolled, updatedSection.getEnrolledCount());
        assertEquals(oldWaitlist, updatedSection.getWaitlistCount());
    }
}