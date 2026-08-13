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

    @GetMapping({"/", "/business-directory"})
    public String home(Model model) {
        model.addAttribute("businesses", businessService.findAll());
        return "business-directory";
    }

    @GetMapping("/business/{id}")
    public String businessDetails(@PathVariable String id, Model model) {
        Optional<Business> found = businessService.findById(id);
        if (found.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("business", found.get());
        return "business-details";
    }
}
