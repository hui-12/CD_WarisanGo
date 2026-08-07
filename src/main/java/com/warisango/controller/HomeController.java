package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/CheckInPage"})
    public String checkIn() {
        return "CheckInPage";
    }

    @GetMapping("/BadgePage")
    public String badges() {
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