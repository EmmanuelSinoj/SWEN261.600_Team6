package com.example.swen_project_v1.web;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final StudentService studentService;

    public AdminController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/admin")
    public String adminHome(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "admin";
    }

    @PostMapping("/admin/create")
    public String createStudent(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String studentId,
                                @RequestParam String email,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.createStudent(firstName, lastName, studentId, email, password);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student " + firstName + " " + lastName + " created successfully!");
        } catch (IllegalArgumentException e) {
            // We split our custom error message string to get the type and the message
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorType", errorParts.length > 1 ? errorParts[0] : "error");
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());

            // Preserve form data so user doesn't have to retype
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("studentId", studentId);
            redirectAttributes.addFlashAttribute("email", email);
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/edit")
    public String editStudent(@RequestParam Long id, Model model) {
        model.addAttribute("student", studentService.getStudentById(id));
        return "edit-student";
    }

    @PostMapping("/admin/update")
    public String updateStudent(@RequestParam Long id,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String studentId,
                                @RequestParam String email,
                                @RequestParam(required = false) String password,
                                @RequestParam(defaultValue = "false") boolean active,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(id, firstName, lastName, studentId, email, password, active);
            redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
        } catch (IllegalArgumentException e) {
            String[] errorParts = e.getMessage().split(":", 2);
            redirectAttributes.addFlashAttribute("errorMessage", errorParts.length > 1 ? errorParts[1] : e.getMessage());
            return "redirect:/admin/edit?id=" + id;
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/disable")
    public String disableStudent(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Student student = studentService.disableStudent(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Student " + student.getFirstName() + " " + student.getLastName() + " has been disabled.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete")
    public String deleteStudent(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Student student = studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Student " + student.getFirstName() + " " + student.getLastName() + " has been permanently deleted.");
        return "redirect:/admin";
    }
}