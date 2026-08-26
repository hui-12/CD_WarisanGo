package com.warisango.controller;

import com.warisango.model.Business;
import com.warisango.model.service.BusinessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class HomeController {

    private final BusinessService businessService;

    public HomeController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping({"/"})
    public String home() {
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
