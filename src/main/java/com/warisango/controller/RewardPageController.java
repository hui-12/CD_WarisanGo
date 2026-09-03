package com.warisango.controller;

import com.warisango.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RewardPageController {

    private final UserService userService;

    public RewardPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/rewards")
    public String rewards(Authentication authentication, Model model) {
        model.addAttribute("activePage", "rewards");
        userService.getUserByUid(authentication.getName()).ifPresent(user -> {
            String owner = user.getName() == null || user.getName().isBlank()
                    ? "My"
                    : user.getName().trim() + "'s";
            model.addAttribute("dashboardTitle", owner + " Rewards Dashboard");
        });
        return "reward";
    }
}
