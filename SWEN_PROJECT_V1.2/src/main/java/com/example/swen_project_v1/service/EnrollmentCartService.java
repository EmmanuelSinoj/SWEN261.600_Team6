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
            throw new IllegalArgumentException("This section is full and cannot be added.");
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
        // 1. Get the student
        Student student = (Student) userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Cart cart = getOrCreateCart(studentEmail);
        List<Section> sections = new ArrayList<>(cart.getSections());

        if (sections.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        // 2. Credit limit check
        // (Note: Waitlisted courses count toward the credit limit so students don't hoard classes)
        int cartCredits = sections.stream()
                .mapToInt(s -> s.getCourse().getCredits())
                .sum();
        int combinedCredits = student.getCurrentCredits() + cartCredits;
        if (combinedCredits > student.getMaxCredits()) {
            throw new IllegalArgumentException(
                    "Credit limit exceeded: enrolling/waitlisting in these courses would bring you to "
                            + combinedCredits + " credits, which exceeds your limit of "
                            + student.getMaxCredits() + "."
            );
        }

        // 3a. Time Conflict Checks
        // Moved this OUTSIDE the loop so it only fetches once!
        List<Enrollment> currentEnrollments = student.getEnrollments();

        for (int i = 0; i < sections.size(); i++) {
            Section cartSection = sections.get(i);

            // -- Cart vs. Cart --
            for (int j = i + 1; j < sections.size(); j++) {
                Section otherCartSection = sections.get(j);
                if (cartSection.hasTimeConflict(otherCartSection)) {
                    throw new IllegalArgumentException(
                            "Time conflict in your cart: " + cartSection.getCourse().getCode() +
                                    " conflicts with " + otherCartSection.getCourse().getCode() + "."
                    );
                }
            }

            // -- Cart vs. Enrolled --
            for (Enrollment enrolled : currentEnrollments) {
                if (cartSection.hasTimeConflict(enrolled.getSection())) {
                    throw new IllegalArgumentException(
                            "Schedule conflict: " + cartSection.getCourse().getCode() +
                                    " conflicts with your already enrolled class " + enrolled.getSection().getCourse().getCode() + "."
                    );
                }
            }
        }

        // 3b. Pre-validate capacities and duplicates
        for (Section section : sections) {
            Section locked = sectionRepository.findByIdWithPessimisticLock(section.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Section " + section.getCrn() + " no longer exists."));

            if (enrollmentRepository.existsByStudentAndSection(student, locked)) {
                throw new IllegalArgumentException(
                        "You are already enrolled or waitlisted in " + locked.getCourse().getCode()
                                + " (" + locked.getCrn() + ").");
            }

            if (enrollmentRepository.isAlreadyEnrolledInCourse(student, locked.getCourse())) {
                throw new IllegalArgumentException(
                        "Checkout failed: You are already enrolled or waitlisted in a different section of "
                                + locked.getCourse().getCode() + ".");
            }

            // ---> NEW WAITLIST CHECK <---
            if (locked.isFull()) {
                // Check if the waitlist is also completely full
                if (locked.getWaitlistCount() >= 10) {
                    throw new IllegalArgumentException(
                            locked.getCourse().getCode() + " (" + locked.getCrn()
                                    + ") is completely full, and the waitlist is closed. Please remove it from your cart.");
                }
            }
        }

        // 4. All checks passed — commit every enrollment/waitlist
        for (Section section : sections) {
            Section locked = sectionRepository.findByIdWithPessimisticLock(section.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Section not found."));

            if (locked.isFull()) {
                // Absolute fallback: Both the class and the waitlist are maxed out
                throw new IllegalArgumentException(
                        locked.getCourse().getCode() + " and its waitlist are completely full."
                );
            }

            EnrollmentStatus finalStatus;

            if (locked.isOpen() && locked.getWaitlistCount() == 0) {
                // 1. There are seats AND nobody is waiting in line
                locked.setEnrolledCount(locked.getEnrolledCount() + 1);
                finalStatus = EnrollmentStatus.ENROLLED;

            } else {
                // 2. Class is either full (isWaitlist() == true) OR there is an open seat but a line exists
                locked.setWaitlistCount(locked.getWaitlistCount() + 1);
                finalStatus = EnrollmentStatus.WAITLISTED;
            }

            sectionRepository.save(locked);

            // Create the official Enrollment record with the correct status
            Enrollment enrollment = new Enrollment(student, locked, finalStatus);
            enrollmentRepository.save(enrollment);

            cart.removeSection(section);
        }

        // 5. Update student's current credit count and clear the cart
        student.setCurrentCredits(student.getCurrentCredits() + cartCredits);
        cartRepository.save(cart);
    }
}
