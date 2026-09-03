package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TouristPageController {

    @GetMapping("/map")
    public String mapPage() {
        return "interactive-map";
    }

    @GetMapping("/saved")
    public String savedPage(Model model) {
        model.addAttribute("activePage", "saved");
        return "saved";
    }

    @GetMapping("/badges")
    public String badgesPage(Model model) {
        model.addAttribute("activePage", "badges");
        return "badge";
    }

    @GetMapping("/challenges")
    public String challengesPage(Model model) {
        model.addAttribute("activePage", "challenges");
        return "challenge";
    }
}
