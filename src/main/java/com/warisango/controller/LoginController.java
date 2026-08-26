package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping({"/", "/login"})
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/heritage-gate")
    public String showAdminLoginPage() {
        return "admin-login";
    }
}
