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

    @GetMapping({"/CheckInPage"})
    public String checkIn(Model model) {
        model.addAttribute("activePage", "checkin");
        return "CheckInPage";
    }
 
    @GetMapping("/badges")
    public String badges(Model model) {
        model.addAttribute("activePage", "badges");
        return "BadgePage";
    }
 
    @GetMapping("/rewards")
    public String rewards(Model model) {
        model.addAttribute("activePage", "rewards");
        return "RewardPage";
    }
 
    @GetMapping("/challenges")
    public String challenges(Model model) {
        model.addAttribute("activePage", "challenges");
        return "ChallengePage";
    }
 
    @GetMapping("/AdminChallengePage")
    public String adminChallenges(Model model) {
        model.addAttribute("activePage", "admin");
        return "admin/AdminChallengePage";
    }
}