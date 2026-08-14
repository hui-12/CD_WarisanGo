package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/"})
    public String home() {
        return "home";
    }

    // Route for the Map page template
    @GetMapping("/map")
    public String mapPage() {
        return "interactiveMap";
    }

    @GetMapping("/CheckInPage")
    public String checkIn() {
        return "CheckInPage";
    }

    @GetMapping("/BadgePage")
    public String badges(Model model) {
        model.addAttribute("activePage", "badges");
        return "BadgePage";
    }

    @GetMapping("/RewardPage")
    public String rewards() {
        return "RewardPage";
    }

    @GetMapping("/ChallengePage")
    public String challenges() {
        return "ChallengePage";
    }

    @GetMapping("/AdminChallengePage")
    public String adminChallenges() {
        return "admin/AdminChallengePage";
    }
}