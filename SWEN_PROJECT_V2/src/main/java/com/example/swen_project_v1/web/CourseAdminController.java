package com.example.swen_project_v1.web;

import com.example.swen_project_v1.course.Course;
import com.example.swen_project_v1.course.DayOfWeek;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.service.CourseService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class CourseAdminController {

    private final CourseService courseService;

    public CourseAdminController(CourseService courseService) {
        this.courseService = courseService;
    }

    // Exception handler for validation errors
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public String handleValidationExceptions(Exception ex, RedirectAttributes redirectAttributes) {
        String message = "Invalid input. Please check all required fields.";

        if (ex instanceof BindException && ((BindException) ex).hasFieldErrors()) {
            message = "Missing required fields. Please fill in all fields.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("days")) {
            message = "Please select at least one day for the section.";
        } else if (ex.getMessage() != null) {
            message = ex.getMessage();
        }

        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses")
    public String coursesHome(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("days", Arrays.asList(DayOfWeek.values()));
        return "admin-courses";
    }

    @PostMapping("/courses/create")
    public String createCourse(@RequestParam String code,
                               @RequestParam String title,
                               @RequestParam int credits,
                               @RequestParam(required = false, defaultValue = "1") Integer minCredits,
                               @RequestParam(required = false, defaultValue = "4") Integer maxCredits,
                               @RequestParam(required = false) String description,
                               RedirectAttributes redirectAttributes) {
        try {
            // Set defaults if not provided
            if (minCredits == null) minCredits = 1;
            if (maxCredits == null) maxCredits = 4;

            courseService.createCourse(code, title, credits, minCredits, maxCredits, description);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Course " + code + " - " + title + " created successfully!");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());

            redirectAttributes.addFlashAttribute("courseCode", code);
            redirectAttributes.addFlashAttribute("courseTitle", title);
            redirectAttributes.addFlashAttribute("courseCredits", credits);
            redirectAttributes.addFlashAttribute("courseDescription", description);
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/update")
    public String updateCourse(@RequestParam Long id,
                               @RequestParam String code,
                               @RequestParam String title,
                               @RequestParam int credits,
                               @RequestParam int minCredits,
                               @RequestParam int maxCredits,
                               @RequestParam(required = false) String description,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.updateCourse(id, code, title, credits, minCredits, maxCredits, description);
            redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully!");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/delete")
    public String deleteCourse(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.getCourseById(id);
            String courseCode = course.getCode();
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Course " + courseCode + " has been deleted.");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/sections/create")
    public String createSection(@RequestParam Long courseId,
                                @RequestParam String crn,
                                @RequestParam(required = false) List<DayOfWeek> days,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                @RequestParam String room,
                                @RequestParam int capacity,
                                @RequestParam String professor,
                                RedirectAttributes redirectAttributes) {
        try {
            // Check if days is null or empty
            if (days == null || days.isEmpty()) {
                throw new IllegalArgumentException("no_days:Please select at least one day for the section.");
            }

            courseService.createSection(courseId, crn, days, startTime, endTime, room, capacity, professor);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Section " + crn + " scheduled successfully!");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());

            redirectAttributes.addFlashAttribute("sectionCrn", crn);
            redirectAttributes.addFlashAttribute("sectionStartTime", startTime);
            redirectAttributes.addFlashAttribute("sectionEndTime", endTime);
            redirectAttributes.addFlashAttribute("sectionRoom", room);
            redirectAttributes.addFlashAttribute("sectionCapacity", capacity);
            redirectAttributes.addFlashAttribute("sectionProfessor", professor);
            redirectAttributes.addFlashAttribute("selectedCourseId", courseId);
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/sections/update")
    public String updateSection(@RequestParam Long id,
                                @RequestParam(required = false) List<DayOfWeek> days,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
                                @RequestParam String room,
                                @RequestParam int capacity,
                                @RequestParam String professor,
                                RedirectAttributes redirectAttributes) {
        try {
            // Check if days is null or empty
            if (days == null || days.isEmpty()) {
                throw new IllegalArgumentException("no_days:Please select at least one day for the section.");
            }

            courseService.updateSection(id, days, startTime, endTime, room, capacity, professor);
            redirectAttributes.addFlashAttribute("successMessage", "Section updated successfully!");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/sections/delete")
    public String deleteSection(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Section section = courseService.getSectionById(id);
            String crn = section.getCrn();
            courseService.deleteSection(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Section " + crn + " has been deleted.");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}