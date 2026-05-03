package com.example.swen_project_v1.service;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.course.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final WaitlistProcessingService waitlistProcessingService;

    public CourseService(CourseRepository courseRepository,
                         SectionRepository sectionRepository,
                         UserRepository userRepository,
                         WaitlistProcessingService waitlistProcessingService) {
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.waitlistProcessingService = waitlistProcessingService;
    }

    // Course operations
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));
    }

    @Transactional
    public Course createCourse(String code, String title, int credits, int minCredits, int maxCredits, String description) {
        if (courseRepository.existsByCode(code)) {
            throw new IllegalArgumentException("duplicate_code:A course with code '" + code + "' already exists.");
        }

        validateCourseCode(code);
        if (courseRepository.existsByCode(code)) {
            throw new IllegalArgumentException("duplicate_code:A course with code '" + code + "' already exists.");
        }
        validateCredits(credits, minCredits, maxCredits);

        Course course = new Course();
        course.setCode(code);
        course.setTitle(title);
        course.setCredits(credits);
        course.setMinCredits(minCredits);
        course.setMaxCredits(maxCredits);
        course.setDescription(description);

        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, String code, String title, int credits, int minCredits, int maxCredits, String description) {
        Course course = getCourseById(id);

        if (!course.getCode().equals(code) && courseRepository.existsByCode(code)) {
            throw new IllegalArgumentException("duplicate_code:Course code '" + code + "' is already in use.");
        }

        validateCredits(credits, minCredits, maxCredits);

        course.setCode(code);
        course.setTitle(title);
        course.setCredits(credits);
        course.setMinCredits(minCredits);
        course.setMaxCredits(maxCredits);
        course.setDescription(description);

        return courseRepository.save(course);
    }

    private void validateCredits(int credits, int minCredits, int maxCredits) {
        if (credits < 1 || credits > 4) {
            throw new IllegalArgumentException("invalid_credits:Credits must be between 1 and 4.");
        }
        if (minCredits < 1 || minCredits > 4) {
            throw new IllegalArgumentException("invalid_min_credits:Minimum credits must be between 1 and 4.");
        }
        if (maxCredits < 1 || maxCredits > 4) {
            throw new IllegalArgumentException("invalid_max_credits:Maximum credits must be between 1 and 4.");
        }
        if (minCredits > maxCredits) {
            throw new IllegalArgumentException("invalid_credit_range:Minimum credits cannot exceed maximum credits.");
        }
        if (credits < minCredits || credits > maxCredits) {
            throw new IllegalArgumentException("credits_out_of_range:Default credits must be between min and max.");
        }
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);

        if (!course.getSections().isEmpty()) {
            throw new IllegalArgumentException("has_sections:Cannot delete course with existing sections. Remove all sections first.");
        }

        courseRepository.delete(course);
    }

    // Section operations
    public List<Section> getSectionsByCourse(Long courseId) {
        return sectionRepository.findByCourseId(courseId);
    }

    public Section getSectionById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found."));
    }

    @Transactional
    public Section createSection(Long courseId, String crn, List<DayOfWeek> days,
                                 LocalTime startTime, LocalTime endTime,
                                 String room, int capacity, String professor,
                                 DeliveryMode deliveryMode) {

        Course course = getCourseById(courseId);

        // Validation: capacity > 0 and <= 30
        if (capacity <= 0) {
            throw new IllegalArgumentException("invalid_capacity:Capacity must be greater than 0.");
        }
        if (capacity > Section.MAX_CAPACITY) {
            throw new IllegalArgumentException("capacity_too_large:Capacity cannot exceed " + Section.MAX_CAPACITY + ".");
        }

        // Validation: at least one day selected
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("no_days:At least one day must be selected.");
        }

        // Validation: end time after start time
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("invalid_time:End time must be after start time.");
        }

        // Validation: unique CRN
        if (sectionRepository.existsByCrn(crn)) {
            throw new IllegalArgumentException("duplicate_crn:Section CRN '" + crn + "' already exists.");
        }

        // Validation: check for schedule conflicts in same room on any of the days
        for (DayOfWeek day : days) {
            List<Section> roomConflicts = sectionRepository.findByDaysContainingAndRoom(day, room);
            for (Section existing : roomConflicts) {
                if (hasTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                    throw new IllegalArgumentException("room_conflict:Room " + room + " is already booked on " + day + " during this time.");
                }
            }
        }

        // Validation: check for professor conflicts on any of the days
        for (DayOfWeek day : days) {
            List<Section> professorConflicts = sectionRepository.findByDaysContainingAndProfessor(day, professor);
            for (Section existing : professorConflicts) {
                if (hasTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                    throw new IllegalArgumentException("professor_conflict:Professor " + professor + " is already teaching on " + day + " during this time.");
                }
            }
        }

        Section section = new Section();
        section.setCourse(course);
        section.setCrn(crn);
        section.setDays(days);
        section.setStartTime(startTime);
        section.setEndTime(endTime);
        section.setRoom(room);
        section.setCapacity(capacity);
        section.setProfessor(professor);
        section.setDeliveryMode(deliveryMode != null ? deliveryMode : DeliveryMode.IN_PERSON);
        section.setEnrolledCount(0);

        Section savedSection = sectionRepository.save(section);
        course.getSections().add(savedSection);

        return savedSection;
    }

    @Transactional
    public Section updateSection(Long sectionId, List<DayOfWeek> days,
                                 LocalTime startTime, LocalTime endTime,
                                 String room, int capacity, String professor,
                                 DeliveryMode deliveryMode) {

        Section section = getSectionById(sectionId);
        int oldCapacity = section.getCapacity();

        // Validation: capacity > 0 and <= 30
        if (capacity <= 0) {
            throw new IllegalArgumentException("invalid_capacity:Capacity must be greater than 0.");
        }
        if (capacity > Section.MAX_CAPACITY) {
            throw new IllegalArgumentException("capacity_too_large:Capacity cannot exceed " + Section.MAX_CAPACITY + ".");
        }

        // Validation: at least one day selected
        if (days == null || days.isEmpty()) {
            throw new IllegalArgumentException("no_days:At least one day must be selected.");
        }

        // Validation: end time after start time
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("invalid_time:End time must be after start time.");
        }

        // Validation: check for schedule conflicts in same room on any of the days (excluding this section)
        for (DayOfWeek day : days) {
            List<Section> roomConflicts = sectionRepository.findByDaysContainingAndRoom(day, room);
            for (Section existing : roomConflicts) {
                if (!existing.getId().equals(sectionId) &&
                        hasTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                    throw new IllegalArgumentException("room_conflict:Room " + room + " is already booked on " + day + " during this time.");
                }
            }
        }

        // Validation: check for professor conflicts on any of the days (excluding this section)
        for (DayOfWeek day : days) {
            List<Section> professorConflicts = sectionRepository.findByDaysContainingAndProfessor(day, professor);
            for (Section existing : professorConflicts) {
                if (!existing.getId().equals(sectionId) &&
                        hasTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                    throw new IllegalArgumentException("professor_conflict:Professor " + professor + " is already teaching on " + day + " during this time.");
                }
            }
        }

        section.setDays(days);
        section.setStartTime(startTime);
        section.setEndTime(endTime);
        section.setRoom(room);
        section.setCapacity(capacity);
        section.setProfessor(professor);
        section.setDeliveryMode(deliveryMode != null ? deliveryMode : DeliveryMode.IN_PERSON);

        Section saved = sectionRepository.save(section);

        if (capacity > oldCapacity) {
            for(int i = 0; i < (capacity - oldCapacity); i++){
                waitlistProcessingService.processWaitlistForSection(saved.getId());}
        }

        return saved;    }

    @Transactional
    public void deleteSection(Long sectionId) {
        // 1. Fetch the section and its course details
        Section section = getSectionById(sectionId);
        int creditsToRefund = section.getCourse().getCredits();

        // 2. Refund credits to all students (Enrolled or Waitlisted)
        // We do this BEFORE deleting the section to ensure data integrity
        for (Enrollment enrollment : section.getEnrollments()) {
            Student student = enrollment.getStudent();

            // Subtract credits from student's profile
            int current = student.getCurrentCredits();
            student.setCurrentCredits(Math.max(0, current - creditsToRefund));

            // Save the student update
            userRepository.save(student);
        }

        // 3. Clean up Cart references
        // This ensures no student has a "ghost" section in their shopping cart
        sectionRepository.deleteLinksInCarts(sectionId);

        // 4. Disconnect from the parent Course object
        Course course = section.getCourse();
        if (course != null) {
            course.getSections().remove(section);
        }

        // 5. Final Delete
        // CascadeType.ALL on the Section-Enrollment relationship will
        // automatically delete the enrollment records for you.
        sectionRepository.delete(section);
    }

    private boolean hasTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private void validateCourseCode(String code) {
        if (code == null || !code.matches("^[A-Z]{4}[0-6][0-9]{2}$")) {
            throw new IllegalArgumentException("invalid_code:Course code must be 4 letters followed by a 3-digit number (max level 699, e.g., SWEN261 or CSCI101).");
        }
    }
}