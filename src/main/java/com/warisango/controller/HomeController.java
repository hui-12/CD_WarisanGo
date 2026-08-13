package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    @GetMapping({"/", "/business-directory"})
    public String home() {
        // With Thymeleaf prefix configured to classpath:/view/ this resolves to /view/business-directory.html
        return "business-directory";
    }

    @GetMapping("/business/{id}")
    public String businessDetails(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        // Sample data - replace with a service/repository lookup in real app
        List<Map<String, Object>> sample = new ArrayList<>();

        Map<String, Object> b1 = new HashMap<>();
        b1.put("id", "b1");
        b1.put("name", "Kedai Nasi Lemak Warisan");
        b1.put("category", "Street Food");
        b1.put("state", "Selangor");
        b1.put("city", "Kuala Lumpur");
        b1.put("rating", 4.6);
        b1.put("photos", new String[]{"/images/business-default.jpg", "/images/hero-banner.jpg"});
        b1.put("address", "No 12, Jalan Heritage, KL");
        b1.put("description", "Traditional nasi lemak served with family sambal recipe.");
        sample.add(b1);

        Map<String, Object> b2 = new HashMap<>();
        b2.put("id", "b2");
        b2.put("name", "Melaka Satay House");
        b2.put("category", "Grill");
        b2.put("state", "Melaka");
        b2.put("city", "Melaka City");
        b2.put("rating", 4.4);
        b2.put("photos", new String[]{"/images/business-default.jpg"});
        b2.put("address", "Lot 5, Jonker Street");
        b2.put("description", "Famous local satay using charcoal grill.");
        sample.add(b2);

        Optional<Map<String, Object>> found = sample.stream().filter(m -> id.equals(m.get("id"))).findFirst();
        if (found.isEmpty()) {
            // If not found, redirect back to the directory with a flash message
            redirectAttributes.addFlashAttribute("message", "Business not found: " + id);
            return "redirect:/";
        }

        model.addAttribute("business", found.get());
        return "business-details";
    }
}
