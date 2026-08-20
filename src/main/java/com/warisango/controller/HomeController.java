package com.warisango.controller;

import org.springframework.stereotype.Controller;
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

}
