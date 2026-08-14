package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BusinessDirectoryController {

    @GetMapping("/business-directory")
    public String showBusinessDirectory() {
        // Returns the Thymeleaf template at src/main/resources/templates/view/business-directory.html
        return "view/business-directory";
    }
}
