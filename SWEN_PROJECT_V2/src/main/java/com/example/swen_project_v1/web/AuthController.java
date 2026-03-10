package com.example.swen_project_v1.web;

import com.example.swen_project_v1.auth.User;
import com.example.swen_project_v1.auth.Role;
import com.example.swen_project_v1.auth.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);
        return "login";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication auth) {
        User user = userRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow();

        if (user.getRole() == Role.ADMINISTRATOR) {
            return "redirect:/admin";
        }
        return "redirect:/student";
    }
}