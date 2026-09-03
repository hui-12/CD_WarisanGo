package com.warisango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/ai-discovery")
    public String aiDiscoveryPage() {
        return "admin/discovery";
    }

    @GetMapping({"/st-discovery", "/admin/ai_discovery"})
    public String legacyDiscoveryPage() {
        return "redirect:/ai-discovery";
    }

    @GetMapping("/admin/pending")
    public String legacyPendingListPage() {
        return "redirect:/admin/pending-list";
    }

    @GetMapping({"/AdminChallengePage", "/admin/challenges"})
    public String adminChallenges(Model model) {
        model.addAttribute("activePage", "admin-challenges");
        return "admin/admin-challenge";
    }

    @GetMapping("/admin/badges")
    public String adminBadges(Model model) {
        model.addAttribute("activePage", "admin-badges");
        return "admin/admin-badge";
    }
}
