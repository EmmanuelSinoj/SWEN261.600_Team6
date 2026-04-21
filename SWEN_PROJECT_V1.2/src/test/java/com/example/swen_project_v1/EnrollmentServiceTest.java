package com.example.swen_project_v1;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.cart.Cart;
import com.example.swen_project_v1.cart.CartRepository;
import com.example.swen_project_v1.course.*;
import com.example.swen_project_v1.service.EnrollmentCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentCartService enrollmentCartService;

    private Student student;
    private Cart cart;
    private final String EMAIL = "test@student.edu";

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setEmail(EMAIL);
        student.setMaxCredits(18);
        student.setCurrentCredits(0);

        cart = new Cart();
        cart.setStudent(student);
    }

    // Helper method to setup basic mocks for a valid user and cart retrieval
    private void setupValidUserAndCart(Section... sections) {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
        when(cartRepository.findByStudent(student)).thenReturn(Optional.of(cart));
        for (Section s : sections) {
            cart.addSection(s);
        }
    }

    private Section createMockSection(Long id, int credits, String code) {
        Section section = spy(new Section());
        section.setId(id);
        Course course = new Course();
        course.setCredits(credits);
        course.setCode(code);
        section.setCourse(course);
        return section;
    }



    @Test
    void checkout_UserNotFound_ThrowsException() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void checkout_EmptyCart_ThrowsException() {
        setupValidUserAndCart(); // No sections added

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertEquals("Your cart is empty.", exception.getMessage());
    }

    @Test
    void checkout_CreditLimitExceeded_ThrowsException() {
        student.setCurrentCredits(15); // Already has 15 credits
        Section heavySection = createMockSection(1L, 4, "CSCI101"); // 4 credits brings total to 19 (Max is 18)
        setupValidUserAndCart(heavySection);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertTrue(exception.getMessage().contains("Credit limit exceeded"));
    }

    @Test
    void checkout_CartTimeConflict_ThrowsException() {
        Section section1 = createMockSection(1L, 3, "CSCI101");
        Section section2 = createMockSection(2L, 3, "MATH101");

        // Force a time conflict
        doReturn(true).when(section1).hasTimeConflict(section2);
        setupValidUserAndCart(section1, section2);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertTrue(exception.getMessage().contains("Time conflict in your cart"));
    }

    @Test
    void checkout_EnrolledTimeConflict_ThrowsException() {
        Section cartSection = createMockSection(1L, 3, "CSCI101");
        Section enrolledSection = createMockSection(2L, 3, "HIST101");

        // Add existing enrollment
        student.getEnrollments().add(new Enrollment(student, enrolledSection, EnrollmentStatus.ENROLLED));

        doReturn(true).when(cartSection).hasTimeConflict(enrolledSection);
        setupValidUserAndCart(cartSection);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertTrue(exception.getMessage().contains("Schedule conflict"));
    }

    @Test
    void checkout_SectionMissing_ThrowsException() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);

        // Simulate DB lock failing because section vanished
        when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertTrue(exception.getMessage().contains("no longer exists"));
    }

    @Test
    void checkout_DuplicateSection_ThrowsException() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);
        when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));

        // Simulate already enrolled in exact section
        when(enrollmentRepository.existsByStudentAndSection(student, section)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertTrue(exception.getMessage().contains("already enrolled or waitlisted"));
    }

    @Test
    void checkout_DuplicateCourse_ThrowsException() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);
        when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));
        when(enrollmentRepository.existsByStudentAndSection(student, section)).thenReturn(false);

        // Simulate already enrolled in different section of same course
        when(enrollmentRepository.isAlreadyEnrolledInCourse(student, section.getCourse())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));
        assertTrue(exception.getMessage().contains("different section of CSCI101"));
    }

    @Test
    void checkout_WaitlistClosed_ThrowsException() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);

        // Simulate full class and maxed waitlist
        doReturn(true).when(section).isSeatFull();
        doReturn(10).when(section).getWaitlistCount();

        lenient().when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));

        lenient().when(enrollmentRepository.isAlreadyEnrolledInCourse(any(), any()))
                .thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                enrollmentCartService.checkoutAllCartItems(EMAIL));

        System.out.println(exception.getMessage());

        assertTrue(exception.getMessage().contains("completely full and the waitlist is closed"));
    }

    @Test
    void checkout_DirectEnrollment_Success() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);

        doReturn(false).when(section).isSeatFull();
        doReturn(true).when(section).isOpen();
        doReturn(0).when(section).getWaitlistCount();
        doReturn(5).when(section).getEnrolledCount();

        lenient().when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));

        lenient().when(enrollmentRepository.existsByStudentAndSection(any(), any())).thenReturn(false);
        lenient().when(enrollmentRepository.isAlreadyEnrolledInCourse(any(), any())).thenReturn(false);

        enrollmentCartService.checkoutAllCartItems(EMAIL);

        // Verify capacity updated correctly
        verify(section).setEnrolledCount(6);

        // Verify official Enrollment record created with ENROLLED status
        verify(enrollmentRepository).save(argThat(enrollment ->
                enrollment.getStatus() == EnrollmentStatus.ENROLLED &&
                        enrollment.getSection().equals(section)
        ));
    }

    @Test
    void checkout_StandardWaitlist_Success() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);

        // Class is NOT open (seats full), but waitlist has room (e.g., 2 people in line)
        doReturn(false).when(section).isSeatFull();
        doReturn(false).when(section).isOpen();
        doReturn(2).when(section).getWaitlistCount();

        lenient().when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));

        lenient().when(enrollmentRepository.existsByStudentAndSection(any(), any())).thenReturn(false);
        lenient().when(enrollmentRepository.isAlreadyEnrolledInCourse(any(), any())).thenReturn(false);

        enrollmentCartService.checkoutAllCartItems(EMAIL);

        // Verify waitlist count increments
        verify(section).setWaitlistCount(3);
        verify(enrollmentRepository).save(argThat(enrollment ->
                enrollment.getStatus() == EnrollmentStatus.WAITLISTED
        ));
    }

    @Test
    void checkout_ForcedWaitlist_Success() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);

        // Class has an open seat, BUT a waitlist line already exists
        doReturn(false).when(section).isSeatFull();
        doReturn(true).when(section).isOpen();
        doReturn(1).when(section).getWaitlistCount();

        lenient().when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));

        lenient().when(enrollmentRepository.existsByStudentAndSection(any(), any())).thenReturn(false);
        lenient().when(enrollmentRepository.isAlreadyEnrolledInCourse(any(), any())).thenReturn(false);

        enrollmentCartService.checkoutAllCartItems(EMAIL);

        // Verify student is forced to waitlist (count increments)
        verify(section).setWaitlistCount(2);
        verify(enrollmentRepository).save(argThat(enrollment ->
                enrollment.getStatus() == EnrollmentStatus.WAITLISTED
        ));
    }

    @Test
    void checkout_FinalVerification_Success() {
        Section section = createMockSection(1L, 3, "CSCI101");
        setupValidUserAndCart(section);

        doReturn(false).when(section).isSeatFull();
        doReturn(true).when(section).isOpen();

        lenient().when(sectionRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(section));

        lenient().when(enrollmentRepository.existsByStudentAndSection(any(), any())).thenReturn(false);
        lenient().when(enrollmentRepository.isAlreadyEnrolledInCourse(any(), any())).thenReturn(false);

        // Initial state
        assertEquals(1, cart.getSections().size());
        assertEquals(0, student.getCurrentCredits());

        enrollmentCartService.checkoutAllCartItems(EMAIL);

        // Verify student credits updated (+3 credits)
        assertEquals(3, student.getCurrentCredits());

        // Verify cart is emptied and saved
        assertTrue(cart.getSections().isEmpty());
        verify(cartRepository).save(cart);
    }
}