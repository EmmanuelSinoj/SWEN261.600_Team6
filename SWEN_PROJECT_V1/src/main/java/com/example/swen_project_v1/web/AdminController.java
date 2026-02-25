package com.example.swen_project_v1.web;

import com.example.swen_project_v1.auth.Student;
import com.example.swen_project_v1.auth.Role;
import com.example.swen_project_v1.auth.StudentRepository;
import com.example.swen_project_v1.auth.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(StudentRepository studentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin")
    public String adminHome(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "admin";
    }

    @PostMapping("/admin/create")
    public String createStudent(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String studentId,
                                @RequestParam String email,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {

        // Check email exists
        if (userRepository.existsByEmailIgnoreCase(email)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "A student with email '" + email + "' already exists. Please use a different email.");
            redirectAttributes.addFlashAttribute("errorType", "duplicate_email");
            // Preserve form data so user doesn't have to retype
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("studentId", studentId);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/admin";
        }

        // Check student ID exists
        if (studentRepository.existsByStudentId(studentId)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Student ID '" + studentId + "' is already taken. Please use a different ID.");
            redirectAttributes.addFlashAttribute("errorType", "duplicate_id");
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("studentId", studentId);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/admin";
        }

        try {
            Student student = new Student();
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setStudentId(studentId);
            student.setEmail(email);
            student.setPasswordHash(passwordEncoder.encode(password));
            student.setRole(Role.STUDENT);
            student.setActive(true);
            student.setCurrentCredits(0);
            student.setMaxCredits(18);

            studentRepository.save(student);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Student " + firstName + " " + lastName + " created successfully!");

        } catch (DataIntegrityViolationException e) {
            // Database constraint violation fallback
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to create student. The email or student ID may already exist.");
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("studentId", studentId);
            redirectAttributes.addFlashAttribute("email", email);
        }

        return "redirect:/admin";
    }

    @GetMapping("/admin/edit")
    public String editStudent(@RequestParam Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        model.addAttribute("student", student);
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

        Student student = studentRepository.findById(id).orElseThrow();

        // Check email changed and new email exists
        if (!student.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot update: Email '" + email + "' is already used by another student.");
            return "redirect:/admin/edit?id=" + id;
        }

        if (!student.getStudentId().equals(studentId) && studentRepository.existsByStudentId(studentId)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot update: Student ID '" + studentId + "' is already taken.");
            return "redirect:/admin/edit?id=" + id;
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setStudentId(studentId);
        student.setEmail(email);
        student.setActive(active);

        if (password != null && !password.isBlank()) {
            student.setPasswordHash(passwordEncoder.encode(password));
        }

        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/disable")
    public String disableStudent(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(id).orElseThrow();
        student.setActive(false);
        studentRepository.save(student);
        redirectAttributes.addFlashAttribute("successMessage",
                "Student " + student.getFirstName() + " " + student.getLastName() + " has been disabled.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete")
    public String deleteStudent(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Student student = studentRepository.findById(id).orElseThrow();
        String fullName = student.getFirstName() + " " + student.getLastName();
        studentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Student " + fullName + " has been permanently deleted.");
        return "redirect:/admin";
    }
}