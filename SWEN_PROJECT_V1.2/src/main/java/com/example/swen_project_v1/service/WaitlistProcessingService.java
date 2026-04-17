package com.example.swen_project_v1.service;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.course.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WaitlistProcessingService {

    private static final Logger log = LoggerFactory.getLogger(WaitlistProcessingService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;

    public WaitlistProcessingService(EnrollmentRepository enrollmentRepository,
                                     SectionRepository sectionRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
    }

    /**
     * Call this whenever a seat opens in a section.
     * Triggered by: drop, capacity increase, or student deletion.
     */
    @Transactional
    public void processWaitlistForSection(Long sectionId) {
        System.out.println("DEBUG: processWaitlistForSection called for section " + sectionId);
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));

        List<Enrollment> waitlist = enrollmentRepository
                .findBySectionIdAndStatusOrderByEnrollmentDateAsc(sectionId, EnrollmentStatus.WAITLISTED);

        if (waitlist.isEmpty()) {
            log.info("No waitlisted students for section {}", sectionId);
            return;
        }

        // Re-fetch section with current counts
        int enrolled = enrollmentRepository.countBySectionIdAndStatus(sectionId, EnrollmentStatus.ENROLLED);
        if (enrolled >= section.getCapacity()) {
            log.info("No available seat in section {} (enrolled={}, capacity={})",
                    sectionId, enrolled, section.getCapacity());
            return;
        }

        // Try each waitlisted student in order until one is successfully promoted
        for (Enrollment candidate : waitlist) {
            boolean promoted = tryPromote(candidate, section);
            if (promoted) {
                // One student promoted per freed seat — stop here.
                // If multiple seats opened (e.g. capacity increase), the caller
                // should call this method once per freed seat, or loop externally.
                break;
            }
        }
    }

    /**
     * Call this BEFORE deleting the student so enrollments are still readable.
     * Processes waitlists for every section the student was ENROLLED in.
     */
    @Transactional
    public void processWaitlistsAfterStudentDeletion(Long deletedStudentId) {
        List<Enrollment> freedEnrollments = enrollmentRepository
                .findByStudentIdAndStatus(deletedStudentId, EnrollmentStatus.ENROLLED);

        log.info("Processing waitlists for {} sections freed by student deletion (studentId={})",
                freedEnrollments.size(), deletedStudentId);

        for (Enrollment e : freedEnrollments) {
            processWaitlistForSection(e.getSection().getId());
        }
    }

    // -----------------------------------------------------------------------

    private boolean tryPromote(Enrollment candidate, Section section) {
        Student student = candidate.getStudent();

        // --- Validation 1: Credit limit (same rule as checkout: combined must not exceed maxCredits) ---
        if (student.getCurrentCredits() > student.getMaxCredits()) {
            log.warn("Skipping student {} — credit limit already exceeded ({}/{})",
                    student.getId(), student.getCurrentCredits(), student.getMaxCredits());
            return false;
        }

        // --- Validation 2: Time conflict against student's currently ENROLLED sections ---
        List<Enrollment> currentEnrollments = enrollmentRepository
                .findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ENROLLED);

        for (Enrollment existing : currentEnrollments) {
            if (section.hasTimeConflict(existing.getSection())) {
                log.warn("Skipping student {} — time conflict between section {} and {}",
                        student.getId(), section.getId(), existing.getSection().getId());
                return false;
            }
        }

        // --- All checks passed: promote to ENROLLED ---
        candidate.setStatus(EnrollmentStatus.ENROLLED);
        enrollmentRepository.save(candidate);

        // Update section counts
        section.setEnrolledCount(section.getEnrolledCount() + 1);
        section.setWaitlistCount(Math.max(0, section.getWaitlistCount() - 1));
        sectionRepository.save(section);

        // Update student credit count (waitlisted credits were already counted at checkout,
        // so no change needed here — credits were reserved when they joined the waitlist)

        log.info("Student {} promoted from WAITLISTED to ENROLLED in section {}",
                student.getId(), section.getId());
        return true;
    }
}