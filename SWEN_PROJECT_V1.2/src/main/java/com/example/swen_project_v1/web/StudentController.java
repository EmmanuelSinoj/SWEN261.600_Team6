package com.example.swen_project_v1.web;

import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.course.DayOfWeek;
import com.example.swen_project_v1.course.DeliveryMode;
import com.example.swen_project_v1.course.SectionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentController {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;

    public StudentController(UserRepository userRepository, SectionRepository sectionRepository) {
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
    }

    // Helper method to keep user data loading DRY
    private void populateBaseModel(Authentication authentication, Model model) {
        User user = this.userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
    }

    @GetMapping("/student")
    public String studentHome(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String professor,
            @RequestParam(required = false) DeliveryMode mode,
            @RequestParam(required = false) String days,
            Authentication authentication, Model model) {

        populateBaseModel(authentication, model);
        model.addAttribute("navCatalog", true);

        // Clean up empty strings from the frontend
        if (query != null && query.isBlank()) query = null;
        if (professor != null && professor.isBlank()) professor = null;

        // Convert 'days' String to DayOfWeek Enum
        DayOfWeek dayEnum = null;
        if (days != null && !days.isBlank()) {
            dayEnum = DayOfWeek.valueOf(days);
        }

        // Convert level (e.g., 100) to a string prefix (e.g., "1")
        String levelPrefix = (level != null) ? String.valueOf(level / 100) : null;

        // Execute the advanced search
        model.addAttribute("sections", sectionRepository.searchCatalog(query, levelPrefix, professor, mode, dayEnum));
        return "student";
    }

    // --- NEW "COMING SOON" ROUTES ---

    @GetMapping("/student/cart")
    public String studentCart(Authentication authentication, Model model) {
        populateBaseModel(authentication, model);
        model.addAttribute("navCart", true);
        model.addAttribute("pageTitle", "Shopping Cart");
        return "coming-soon";
    }

    @GetMapping("/student/enrolled")
    public String studentEnrolled(Authentication authentication, Model model) {
        populateBaseModel(authentication, model);
        model.addAttribute("navEnrolled", true);
        model.addAttribute("pageTitle", "Enrolled Courses");
        return "coming-soon";
    }

    @GetMapping("/student/timetable")
    public String studentTimetable(Authentication authentication, Model model) {
        populateBaseModel(authentication, model);
        model.addAttribute("navTimetable", true);
        model.addAttribute("pageTitle", "My Timetable");
        return "coming-soon";
    }
}