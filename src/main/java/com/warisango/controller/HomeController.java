package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/business-directory"})
    public String home() {
        return "Homepage";
    }

    // Route for the Map page template
    @GetMapping("/map")
    public String mapPage(Model model) {
        model.addAttribute("pageTitle", "WarisanGo - Interactive Map");
        return "InteractiveMapPage";
    }
}