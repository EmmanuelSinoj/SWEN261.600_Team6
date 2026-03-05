package com.example.swen_project_v1.web;

import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
public class StudentController {
    private final UserRepository userRepository;

    public StudentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/student")
    public String studentHome(Authentication authentication, Model model) {
        //placeholder dashboard
        User user = this.userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();

        model.addAttribute("firstName",user.getFirstName());
        model.addAttribute("lastName",user.getLastName());
        return "student";
    }
}