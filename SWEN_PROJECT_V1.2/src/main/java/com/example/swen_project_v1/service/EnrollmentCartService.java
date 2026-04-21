package com.example.swen_project_v1.service;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.cart.Cart;
import com.example.swen_project_v1.cart.CartRepository;
import com.example.swen_project_v1.course.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EnrollmentCartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentCartService(CartRepository cartRepository,
                                 UserRepository userRepository,
                                 SectionRepository sectionRepository, EnrollmentRepository enrollmentRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public List<Section> getCartSections(String studentEmail) {
        return getOrCreateCart(studentEmail).getSections();
    }

    @Transactional
    public void addToCart(String studentEmail, Long sectionId) {
        Cart cart = getOrCreateCart(studentEmail);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found."));

        // same exact section already added
        if (cart.getSections().stream().anyMatch(s -> s.getId().equals(sectionId))) {
            throw new IllegalArgumentException("This section is already in your cart.");
        }

        // prevent two sections of the same course
        if (cart.getSections().stream().anyMatch(s ->
                s.getCourse().getId().equals(section.getCourse().getId()))) {
            throw new IllegalArgumentException(
                    "You already have another section of " + section.getCourse().getCode() + " in your cart."
            );
        }

        // prevent full section
        if (section.isFull()) {
            throw new IllegalArgumentException(
                    "This section is completely full and the waitlist is closed. Cannot be added.");
        }

        // prevent time conflict
        for (Section existing : cart.getSections()) {
            if (existing.hasTimeConflict(section)) {
                throw new IllegalArgumentException(
                        "Time conflict with " + existing.getCourse().getCode() + " (" + existing.getCrn() + ")."
                );
            }
        }

        cart.addSection(section);
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(String studentEmail, Long sectionId) {
        Cart cart = getOrCreateCart(studentEmail);

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found."));

        cart.removeSection(section);
        cartRepository.save(cart);
    }
    @Transactional
    public void dropEnrollment(String studentEmail, Long sectionId) {
        Student student = (Student) userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Section section = sectionRepository.findByIdWithPessimisticLock(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found."));

        // Find the active enrollment (either ENROLLED or WAITLISTED)
        Enrollment enrollmentToDrop = student.getEnrollments().stream()
                .filter(e -> e.getSection().getId().equals(sectionId))
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED || e.getStatus() == EnrollmentStatus.WAITLISTED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("You are not enrolled or waitlisted in this section."));

        // Keep track of their status before we change it to DROPPED
        EnrollmentStatus originalStatus = enrollmentToDrop.getStatus();

        enrollmentToDrop.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollmentToDrop);

        // Decrement the appropriate counter based on what they just dropped
        if (originalStatus == EnrollmentStatus.ENROLLED) {
            if (section.getEnrolledCount() > 0) {
                section.setEnrolledCount(section.getEnrolledCount() - 1);
            }
        } else if (originalStatus == EnrollmentStatus.WAITLISTED) {
            if (section.getWaitlistCount() > 0) {
                section.setWaitlistCount(section.getWaitlistCount() - 1);
            }
        }

        // Refund the credits (since checkoutAllCartItems charges credits for both waitlisted and enrolled courses)
        if (student.getCurrentCredits() >= section.getCourse().getCredits()) {
            student.setCurrentCredits(student.getCurrentCredits() - section.getCourse().getCredits());
        }

        sectionRepository.saveAndFlush(section);
        userRepository.save(student);

        // Trigger US-09B automatic waitlist processing
        // Note: We only need to process the waitlist if an ENROLLED student dropped, opening up a seat!
        if (originalStatus == EnrollmentStatus.ENROLLED) {
            processWaitlistForSection(sectionId);
        }
    }
    @Transactional
    public void processWaitlistForSection(Long sectionId) {
        Section section = sectionRepository.findByIdWithPessimisticLock(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found."));

        if (!section.isOpen()) {
            return;
        }

        List<Enrollment> waitlisted = enrollmentRepository
                .findBySectionIdAndStatusOrderByEnrollmentDateAsc(sectionId, EnrollmentStatus.WAITLISTED);

        for (Enrollment candidate : waitlisted) {
            Student waitlistedStudent = candidate.getStudent();

            int projectedCredits = waitlistedStudent.getCurrentCredits() + section.getCourse().getCredits();
            if (projectedCredits > waitlistedStudent.getMaxCredits()) {
                continue;
            }

            List<Enrollment> enrolledSections = enrollmentRepository
                    .findByStudentIdAndStatus(waitlistedStudent.getId(), EnrollmentStatus.ENROLLED);

            boolean hasConflict = enrolledSections.stream()
                    .anyMatch(e -> e.getSection().hasTimeConflict(section));

            if (hasConflict) {
                continue;
            }

            candidate.setStatus(EnrollmentStatus.ENROLLED);
            enrollmentRepository.save(candidate);

            section.setWaitlistCount(Math.max(0, section.getWaitlistCount() - 1));
            section.setEnrolledCount(section.getEnrolledCount() + 1);
            sectionRepository.saveAndFlush(section);

            waitlistedStudent.setCurrentCredits(waitlistedStudent.getCurrentCredits() + section.getCourse().getCredits());
            userRepository.save(waitlistedStudent);

            break;
        }
    }
    @Transactional
    public Cart getOrCreateCart(String studentEmail) {
        User user = userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!(user instanceof Student student)) {
            throw new IllegalArgumentException("Only students can have carts.");
        }

        return cartRepository.findByStudent(student).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setStudent(student);
            return cartRepository.save(cart);
        });
    }


    @Transactional
    public void checkoutAllCartItems(String studentEmail) {
        System.out.println("DEBUG: checkoutAllCartItems started for " + studentEmail);
        Student student = (Student) userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Cart cart = getOrCreateCart(studentEmail);
        List<Section> sections = new ArrayList<>(cart.getSections());

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        // 1. Credit limit check
        int cartCredits = sections.stream()
                .mapToInt(s -> s.getCourse().getCredits())
                .sum();
        int combinedCredits = student.getCurrentCredits() + cartCredits;
        if (combinedCredits > student.getMaxCredits()) {
            throw new IllegalArgumentException(
                    "Credit limit exceeded: enrolling/waitlisting in these courses would bring you to "
                            + combinedCredits + " credits, which exceeds your limit of "
                            + student.getMaxCredits() + ".");
        }

        // 2. Time conflict checks (cart vs cart, cart vs enrolled)
        // AFTER
        List<Enrollment> currentEnrollments = student.getEnrollments().stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED || e.getStatus() == EnrollmentStatus.WAITLISTED)
                .toList();        for (int i = 0; i < sections.size(); i++) {
            Section cartSection = sections.get(i);
            for (int j = i + 1; j < sections.size(); j++) {
                Section otherCartSection = sections.get(j);
                if (cartSection.hasTimeConflict(otherCartSection)) {
                    throw new IllegalArgumentException(
                            "Time conflict in your cart: " + cartSection.getCourse().getCode()
                                    + " conflicts with " + otherCartSection.getCourse().getCode() + ".");
                }
            }
            for (Enrollment enrolled : currentEnrollments) {
                if (cartSection.hasTimeConflict(enrolled.getSection())) {
                    throw new IllegalArgumentException(
                            "Schedule conflict: " + cartSection.getCourse().getCode()
                                    + " conflicts with your already enrolled class "
                                    + enrolled.getSection().getCourse().getCode() + ".");
                }
            }
        }

        // 3. Lock, validate, and commit — all in ONE loop (no double-fetch)
        for (Section section : sections) {
            Section locked = sectionRepository.findByIdWithPessimisticLock(section.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Section " + section.getCrn() + " no longer exists."));

            // Duplicate check
            if (enrollmentRepository.existsByStudentAndSection(student, locked)) {
                throw new IllegalArgumentException(
                        "You are already enrolled or waitlisted in "
                                + locked.getCourse().getCode() + " (" + locked.getCrn() + ").");
            }
            if (enrollmentRepository.isAlreadyEnrolledInCourse(student, locked.getCourse())) {
                throw new IllegalArgumentException(
                        "You are already enrolled in a different section of "
                                + locked.getCourse().getCode() + ".");
            }

            // Waitlist-closed check
            if (locked.isSeatFull() && locked.getWaitlistCount() >= Section.WAITLIST_CAP) {
                throw new IllegalArgumentException(
                        locked.getCourse().getCode() + " (" + locked.getCrn()
                                + ") is completely full and the waitlist is closed. Please remove it from your cart.");
            }

            // Determine status and update counts
            EnrollmentStatus finalStatus;
            if (locked.isOpen() && locked.getWaitlistCount() == 0) {
                locked.setEnrolledCount(locked.getEnrolledCount() + 1);
                finalStatus = EnrollmentStatus.ENROLLED;
            } else {
                locked.setWaitlistCount(locked.getWaitlistCount() + 1);
                finalStatus = EnrollmentStatus.WAITLISTED;
            }

            sectionRepository.saveAndFlush(locked); // <-- saveAndFlush, not just save

            Enrollment enrollment = new Enrollment(student, locked, finalStatus);
            enrollmentRepository.save(enrollment);

            cart.removeSection(section);
        }

        // 4. Update student credits and save cart
        student.setCurrentCredits(student.getCurrentCredits() + cartCredits);
        userRepository.save(student);   // <-- also save the student!
        cartRepository.save(cart);
    }
}
