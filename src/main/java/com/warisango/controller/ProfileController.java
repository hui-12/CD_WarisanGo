package com.warisango.controller;

import com.warisango.model.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        authService.findUserByUid(authentication.getName()).ifPresent(user -> model.addAttribute("user", user));
        return "ProfilePage";
    }
}
