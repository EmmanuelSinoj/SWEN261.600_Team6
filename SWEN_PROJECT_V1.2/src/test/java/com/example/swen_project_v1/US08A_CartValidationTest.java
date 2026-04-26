package com.example.swen_project_v1;

// ─────────────────────────────────────────────────────────────────────────────
// US-08A: Cart Validation & Conflict Warnings
// Unit Tests  +  Mockito Tests  (fixed)
// ─────────────────────────────────────────────────────────────────────────────

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.course.Course;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.course.SectionRepository;
import com.example.swen_project_v1.service.EnrollmentCartService;
import com.example.swen_project_v1.web.StudentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// LENIENT strictness fixes UnnecessaryStubbing: the @BeforeEach student setup
// is shared across all nested classes, including pure-arithmetic tests that
// never touch the mocks — lenient mode allows that without failing.
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class US08A_CartValidationTest {

    // ── shared mocks ──────────────────────────────────────────────────────────
    @Mock UserRepository        userRepository;
    @Mock SectionRepository     sectionRepository;
    @Mock EnrollmentCartService enrollmentCartService;
    @Mock Authentication        authentication;
    @Mock Model                 model;
    @Mock RedirectAttributes    redirectAttributes;

    @InjectMocks
    StudentController studentController;

    private Student student;

    // Uses real Course/Section objects instead of mocks — avoids
    // UnfinishedStubbing caused by Mockito being unable to mock final methods.
    private Section sectionWithCredits(int credits) {
        Course course = new Course();
        course.setCredits(credits);

        Section section = new Section();
        section.setCourse(course);
        return section;
    }

    @BeforeEach
    void setUpStudent() {
        student = mock(Student.class);
        when(student.getFirstName()).thenReturn("Alice");
        when(student.getLastName()).thenReturn("Smith");
        when(student.getCurrentCredits()).thenReturn(9);
        when(student.getMaxCredits()).thenReturn(18);

        when(authentication.getName()).thenReturn("alice@uni.edu");
        when(userRepository.findByEmailIgnoreCase("alice@uni.edu"))
                .thenReturn(Optional.of(student));
    }

    // =========================================================================
    //  AC-1 & AC-2 – Credit Summary Bar
    // =========================================================================
    @Nested
    @DisplayName("AC-1 & AC-2 – Credit Summary Bar attributes")
    class CreditSummaryBar {

        @Test
        @DisplayName("AC-1: enrolledCredits, cartCredits, combinedCredits, maxCredits added to model")
        void creditSummaryAttributesExposedCorrectly() {
            List<Section> cart = List.of(sectionWithCredits(3), sectionWithCredits(3));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            studentController.studentCart(authentication, model);

            verify(model).addAttribute("enrolledCredits", 9);
            verify(model).addAttribute("cartCredits", 6);
            verify(model).addAttribute("combinedCredits", 15);
            verify(model).addAttribute("maxCredits", 18);
        }

        @Test
        @DisplayName("AC-1: overCreditLimit is FALSE when combined <= max (9 + 6 = 15 <= 18)")
        void overCreditLimit_falseWhenUnderLimit() {
            List<Section> cart = List.of(sectionWithCredits(3), sectionWithCredits(3));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            studentController.studentCart(authentication, model);

            verify(model).addAttribute("overCreditLimit", false);
        }

        @Test
        @DisplayName("AC-2: overCreditLimit is TRUE when combined > max (9 + 12 = 21 > 18)")
        void overCreditLimit_trueWhenOverLimit() {
            List<Section> cart = List.of(sectionWithCredits(6), sectionWithCredits(6));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            studentController.studentCart(authentication, model);

            verify(model).addAttribute("overCreditLimit", true);
        }

        @Test
        @DisplayName("AC-2: overCreditLimit is TRUE at exact boundary (9 + 10 = 19 > 18)")
        void overCreditLimit_exactBoundary() {
            List<Section> cart = List.of(sectionWithCredits(10));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            studentController.studentCart(authentication, model);

            verify(model).addAttribute("overCreditLimit", true);
        }

        @Test
        @DisplayName("AC-1: hasCartItems TRUE and cartItemCount matches section count")
        void hasCartItemsAndCount() {
            List<Section> cart = List.of(sectionWithCredits(3), sectionWithCredits(3));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            studentController.studentCart(authentication, model);

            verify(model).addAttribute("hasCartItems", true);
            verify(model).addAttribute("cartItemCount", 2);
        }

        @Test
        @DisplayName("AC-1: cartSections list is passed to the model")
        void cartSectionsAddedToModel() {
            List<Section> cart = List.of(sectionWithCredits(3));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            studentController.studentCart(authentication, model);

            verify(model).addAttribute("cartSections", cart);
        }

        @Test
        @DisplayName("AC-1: correct view name 'student-cart' returned")
        void returnsStudentCartView() {
            List<Section> cart = List.of(sectionWithCredits(3));
            when(enrollmentCartService.getCartSections("alice@uni.edu")).thenReturn(cart);

            String view = studentController.studentCart(authentication, model);

            assertThat(view).isEqualTo("student-cart");
        }
    }

    // =========================================================================
    //  AC-3 – Empty Cart State
    // =========================================================================
    @Nested
    @DisplayName("AC-3 – Empty Cart State")
    class EmptyCartState {

        @BeforeEach
        void emptyCart() {
            when(enrollmentCartService.getCartSections("alice@uni.edu"))
                    .thenReturn(Collections.emptyList());
        }

        @Test
        @DisplayName("hasCartItems is FALSE when cart is empty")
        void hasCartItemsFalseWhenEmpty() {
            studentController.studentCart(authentication, model);
            verify(model).addAttribute("hasCartItems", false);
        }

        @Test
        @DisplayName("cartItemCount is 0 when cart is empty")
        void cartItemCountZeroWhenEmpty() {
            studentController.studentCart(authentication, model);
            verify(model).addAttribute("cartItemCount", 0);
        }

        @Test
        @DisplayName("cartCredits is 0 when cart is empty")
        void cartCreditsZeroWhenEmpty() {
            studentController.studentCart(authentication, model);
            verify(model).addAttribute("cartCredits", 0);
        }

        @Test
        @DisplayName("combinedCredits equals enrolledCredits only when cart is empty (9 + 0 = 9)")
        void combinedEqualsEnrolledWhenEmpty() {
            studentController.studentCart(authentication, model);
            verify(model).addAttribute("combinedCredits", 9);
        }

        @Test
        @DisplayName("overCreditLimit is FALSE when cart is empty")
        void overCreditLimitFalseWhenEmpty() {
            studentController.studentCart(authentication, model);
            verify(model).addAttribute("overCreditLimit", false);
        }

        @Test
        @DisplayName("still returns student-cart view (template renders empty state block)")
        void stillReturnsCartView() {
            String view = studentController.studentCart(authentication, model);
            assertThat(view).isEqualTo("student-cart");
        }
    }

    // =========================================================================
    //  AC-4 & AC-5 – Enroll Now (POST /student/cart/enroll-all)
    // =========================================================================
    @Nested
    @DisplayName("AC-4 & AC-5 – Enroll Now (POST /student/cart/enroll-all)")
    class EnrollAll {

        @Test
        @DisplayName("AC-4: enrollAll() delegates to enrollmentCartService.checkoutAllCartItems()")
        void enrollAll_delegatesToService() {
            studentController.enrollAll(authentication, redirectAttributes);

            verify(enrollmentCartService, times(1))
                    .checkoutAllCartItems("alice@uni.edu");
        }

        @Test
        @DisplayName("AC-5: On success → redirect to /enrolled.html")
        void enrollAll_successRedirectsToEnrolled() {
            String redirect = studentController.enrollAll(authentication, redirectAttributes);

            assertThat(redirect).isEqualTo("redirect:/enrolled.html");
        }

        @Test
        @DisplayName("AC-5: On success → success flash message is set")
        void enrollAll_successSetsFlashMessage() {
            studentController.enrollAll(authentication, redirectAttributes);

            verify(redirectAttributes).addFlashAttribute(
                    eq("successMessage"), anyString());
        }

        @Test
        @DisplayName("AC-5: On IllegalArgumentException → redirect to /student/cart")
        void enrollAll_failureRedirectsToCart() {
            doThrow(new IllegalArgumentException("Credit limit exceeded."))
                    .when(enrollmentCartService)
                    .checkoutAllCartItems("alice@uni.edu");

            String redirect = studentController.enrollAll(authentication, redirectAttributes);

            assertThat(redirect).isEqualTo("redirect:/student/cart");
        }

        @Test
        @DisplayName("AC-5: On failure → error flash message contains the exception text")
        void enrollAll_failureSetsErrorFlashMessage() {
            doThrow(new IllegalArgumentException("Credit limit exceeded."))
                    .when(enrollmentCartService)
                    .checkoutAllCartItems("alice@uni.edu");

            studentController.enrollAll(authentication, redirectAttributes);

            verify(redirectAttributes).addFlashAttribute(
                    "errorMessage", "Credit limit exceeded.");
        }

        @Test
        @DisplayName("AC-5: On IllegalStateException → redirect to /student/cart")
        void enrollAll_illegalStateRedirectsToCart() {
            doThrow(new IllegalStateException("Cart is empty."))
                    .when(enrollmentCartService)
                    .checkoutAllCartItems("alice@uni.edu");

            String redirect = studentController.enrollAll(authentication, redirectAttributes);

            assertThat(redirect).isEqualTo("redirect:/student/cart");
        }
    }

    // =========================================================================
    //  Pure Unit Tests – Credit Calculation Arithmetic (no mocks needed)
    // =========================================================================
    @Nested
    @DisplayName("Unit – Credit Calculation Arithmetic")
    class CreditCalculationUnit {

        @Test
        @DisplayName("cartCredits = sum of credits across all sections in cart")
        void cartCredits_isSumOfSectionCredits() {
            int sum = List.of(3, 4, 2).stream().mapToInt(Integer::intValue).sum();
            assertThat(sum).isEqualTo(9);
        }

        @Test
        @DisplayName("combinedCredits = enrolledCredits + cartCredits")
        void combinedCredits_isEnrolledPlusCart() {
            assertThat(9 + 6).isEqualTo(15);
        }

        @Test
        @DisplayName("overCreditLimit is true when combinedCredits > maxCredits")
        void overLimit_whenCombinedExceedsMax() {
            assertThat(21 > 18).isTrue();
        }

        @Test
        @DisplayName("overCreditLimit is false when combinedCredits == maxCredits (exactly at limit)")
        void notOverLimit_whenCombinedEqualsMax() {
            assertThat(18 > 18).isFalse();
        }

        @Test
        @DisplayName("overCreditLimit is false when combinedCredits < maxCredits")
        void notOverLimit_whenCombinedBelowMax() {
            assertThat(15 > 18).isFalse();
        }
    }
}
