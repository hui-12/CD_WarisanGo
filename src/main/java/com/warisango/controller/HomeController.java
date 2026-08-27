package com.warisango.controller;

import com.warisango.model.service.BusinessService;
import com.warisango.model.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BusinessService businessService;
    private final UserService userService;

    public HomeController(BusinessService businessService, UserService userService) {
        this.businessService = businessService;
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        userService.getUserByUid(authentication.getName())
                .ifPresent(user -> model.addAttribute("currentUser", user));
        return "home";
    }

    // Route for the Map page template
    @GetMapping("/map")
    public String mapPage() {
        return "interactiveMap";
    }

    @GetMapping("/saved")
    public String savedPage(Model model) {
        model.addAttribute("activePage", "saved");
        return "SavedPage"; 
    }
    
    @GetMapping("/badges")
    public String badges(Model model) {
        model.addAttribute("activePage", "badges");
        return "BadgePage";
    }
 
    @GetMapping("/rewards")
    public String rewards(Authentication authentication, Model model) {
        model.addAttribute("activePage", "rewards");
        userService.getUserByUid(authentication.getName()).ifPresent(user -> {
            String displayName = user.getName() == null || user.getName().isBlank()
                    ? "My" : user.getName().trim() + "'s";
            model.addAttribute("dashboardTitle", displayName + " Rewards Dashboard");
        });
        return "RewardPage";
    }
 
    @GetMapping("/challenges")
    public String challenges(Model model) {
        model.addAttribute("activePage", "challenges");
        return "ChallengePage";
    }
 
    @GetMapping({"/AdminChallengePage", "/admin/challenges"})
    public String adminChallenges(Model model) {
        model.addAttribute("activePage", "admin-challenges");
        return "admin/AdminChallengePage";
    }

    @GetMapping("/admin/badges")
    public String adminBadges(Model model) {
        model.addAttribute("activePage", "admin-badges");
        return "admin/AdminBadgePage";
    }
}
