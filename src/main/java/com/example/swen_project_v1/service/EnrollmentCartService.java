package com.example.swen_project_v1.service;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.cart.Cart;
import com.example.swen_project_v1.cart.CartRepository;
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

    public EnrollmentCartService(CartRepository cartRepository,
                                 UserRepository userRepository,
                                 SectionRepository sectionRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
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
}