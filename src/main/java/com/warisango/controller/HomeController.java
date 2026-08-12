package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/business-directory"})
    public String home() {
        return "admin/AIDiscoveryPage.html";
    }
}