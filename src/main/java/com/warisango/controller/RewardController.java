package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RewardController {

    @GetMapping("/")
    public String home() {
        return "resources/view/CheckInPage.html";
    }
}