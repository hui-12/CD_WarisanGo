package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BusinessDirectoryController {

    @RequestMapping("/directory")
    public String showBusinessDirectory() {
        // Returns the Thymeleaf template at src/main/resources/templates/view/BusinessDirectoryPage.html
        return "BusinessDirectoryPage";
    }
}
