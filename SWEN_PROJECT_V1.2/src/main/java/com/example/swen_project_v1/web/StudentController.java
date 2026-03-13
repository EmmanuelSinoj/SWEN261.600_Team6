package com.example.swen_project_v1.web;

import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.UserRepository;
import com.example.swen_project_v1.course.DayOfWeek;
import com.example.swen_project_v1.course.DeliveryMode;
import com.example.swen_project_v1.course.Section;
import com.example.swen_project_v1.course.SectionRepository;
import com.example.swen_project_v1.service.EnrollmentCartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StudentController {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentCartService enrollmentCartService;

    public StudentController(UserRepository userRepository,
                             SectionRepository sectionRepository,
                             EnrollmentCartService enrollmentCartService) {
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentCartService = enrollmentCartService;
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

        if (query != null && query.isBlank()) query = null;
        if (professor != null && professor.isBlank()) professor = null;

        DayOfWeek dayEnum = null;
        if (days != null && !days.isBlank()) {
            dayEnum = DayOfWeek.valueOf(days);
        }

        String levelPrefix = (level != null) ? String.valueOf(level / 100) : null;

        model.addAttribute("sections", sectionRepository.searchCatalog(query, levelPrefix, professor, mode, dayEnum));
        return "student";
    }

    @PostMapping("/student/cart/add")
    public String addToCart(@RequestParam Long sectionId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            enrollmentCartService.addToCart(authentication.getName(), sectionId);
            redirectAttributes.addFlashAttribute("successMessage", "Section added to cart successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student";
    }

    @GetMapping("/student/cart")
    public String studentCart(Authentication authentication, Model model) {
        populateBaseModel(authentication, model);
        model.addAttribute("navCart", true);

        List<Section> cartSections = enrollmentCartService.getCartSections(authentication.getName());
        model.addAttribute("cartSections", cartSections);

        return "student-cart";
    }

    @PostMapping("/student/cart/remove")
    public String removeFromCart(@RequestParam Long sectionId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        enrollmentCartService.removeFromCart(authentication.getName(), sectionId);
        redirectAttributes.addFlashAttribute("successMessage", "Section removed from cart.");
        return "redirect:/student/cart";
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