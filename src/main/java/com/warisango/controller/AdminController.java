package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping({"/ai-discovery", "/st-discovery"})
    public String aiDiscoveryPage() {
        return "admin/AIDiscoveryPage";
    }
}
