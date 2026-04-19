package com.example.swen_project_v1;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.cart.CartRepository;
import com.example.swen_project_v1.course.Course;
import com.example.swen_project_v1.course.Enrollment;
import com.example.swen_project_v1.course.EnrollmentRepository;
import com.example.swen_project_v1.course.EnrollmentStatus;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.course.SectionRepository;
import com.example.swen_project_v1.service.EnrollmentCartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class US09ADropCourseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Spy
    @InjectMocks
    private EnrollmentCartService enrollmentCartService;

    @Mock
    private Student student;

    @Mock
    private Section section;

    @Mock
    private Course course;

    @Mock
    private Enrollment enrollment;

    private static final String EMAIL = "student@test.com";
    private static final Long STUDENT_ID = 1L;
    private static final Long SECTION_ID = 100L;

    @Test
    void dropEnrollment_success() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
        when(student.getId()).thenReturn(STUDENT_ID);

        when(sectionRepository.findByIdWithPessimisticLock(SECTION_ID))
                .thenReturn(Optional.of(section));

        when(enrollmentRepository.findByStudentIdAndStatus(STUDENT_ID, EnrollmentStatus.ENROLLED))
                .thenReturn(List.of(enrollment));

        when(enrollment.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(SECTION_ID);

        when(section.getEnrolledCount()).thenReturn(5);
        when(section.getCourse()).thenReturn(course);
        when(course.getCredits()).thenReturn(3);

        when(student.getCurrentCredits()).thenReturn(12);

        doNothing().when(enrollmentCartService).processWaitlistForSection(SECTION_ID);

        enrollmentCartService.dropEnrollment(EMAIL, SECTION_ID);

        verify(enrollment).setStatus(EnrollmentStatus.DROPPED);
        verify(enrollmentRepository).save(enrollment);
        verify(section).setEnrolledCount(4);
        verify(student).setCurrentCredits(9);
        verify(sectionRepository).saveAndFlush(section);
        verify(userRepository).save(student);
        verify(enrollmentCartService).processWaitlistForSection(SECTION_ID);
    }

    @Test
    void dropEnrollment_userNotFound() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentCartService.dropEnrollment(EMAIL, SECTION_ID)
        );

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void dropEnrollment_sectionNotFound() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
        when(sectionRepository.findByIdWithPessimisticLock(SECTION_ID))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentCartService.dropEnrollment(EMAIL, SECTION_ID)
        );

        assertEquals("Section not found.", exception.getMessage());
    }

    @Test
    void dropEnrollment_notEnrolledInSection() {
        Enrollment otherEnrollment = mock(Enrollment.class);
        Section otherSection = mock(Section.class);

        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
        when(student.getId()).thenReturn(STUDENT_ID);

        when(sectionRepository.findByIdWithPessimisticLock(SECTION_ID))
                .thenReturn(Optional.of(section));

        when(enrollmentRepository.findByStudentIdAndStatus(STUDENT_ID, EnrollmentStatus.ENROLLED))
                .thenReturn(List.of(otherEnrollment));

        when(otherEnrollment.getSection()).thenReturn(otherSection);
        when(otherSection.getId()).thenReturn(999L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> enrollmentCartService.dropEnrollment(EMAIL, SECTION_ID)
        );

        assertEquals("You are not enrolled in this section.", exception.getMessage());
    }

    @Test
    void dropEnrollment_doesNotReduceEnrolledCountBelowZero() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
        when(student.getId()).thenReturn(STUDENT_ID);

        when(sectionRepository.findByIdWithPessimisticLock(SECTION_ID))
                .thenReturn(Optional.of(section));

        when(enrollmentRepository.findByStudentIdAndStatus(STUDENT_ID, EnrollmentStatus.ENROLLED))
                .thenReturn(List.of(enrollment));

        when(enrollment.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(SECTION_ID);

        when(section.getEnrolledCount()).thenReturn(0);
        when(section.getCourse()).thenReturn(course);
        when(course.getCredits()).thenReturn(3);

        when(student.getCurrentCredits()).thenReturn(12);

        doNothing().when(enrollmentCartService).processWaitlistForSection(SECTION_ID);

        enrollmentCartService.dropEnrollment(EMAIL, SECTION_ID);

        verify(section, never()).setEnrolledCount(anyInt());
        verify(student).setCurrentCredits(9);
        verify(sectionRepository).saveAndFlush(section);
        verify(userRepository).save(student);
    }

    @Test
    void dropEnrollment_doesNotReduceCreditsBelowZero() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(student));
        when(student.getId()).thenReturn(STUDENT_ID);

        when(sectionRepository.findByIdWithPessimisticLock(SECTION_ID))
                .thenReturn(Optional.of(section));

        when(enrollmentRepository.findByStudentIdAndStatus(STUDENT_ID, EnrollmentStatus.ENROLLED))
                .thenReturn(List.of(enrollment));

        when(enrollment.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(SECTION_ID);

        when(section.getEnrolledCount()).thenReturn(3);
        when(section.getCourse()).thenReturn(course);
        when(course.getCredits()).thenReturn(3);

        when(student.getCurrentCredits()).thenReturn(2);

        doNothing().when(enrollmentCartService).processWaitlistForSection(SECTION_ID);

        enrollmentCartService.dropEnrollment(EMAIL, SECTION_ID);

        verify(section).setEnrolledCount(2);
        verify(student, never()).setCurrentCredits(anyInt());
        verify(sectionRepository).saveAndFlush(section);
        verify(userRepository).save(student);
    }
}