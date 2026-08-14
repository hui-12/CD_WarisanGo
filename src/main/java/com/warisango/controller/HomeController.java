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

}
