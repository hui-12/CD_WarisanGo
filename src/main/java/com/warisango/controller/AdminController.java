package com.warisango.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final boolean localDiscoveryEnabled;

    public AdminController(
            @Value("${warisango.discovery.local-enabled:false}") boolean localDiscoveryEnabled) {
        this.localDiscoveryEnabled = localDiscoveryEnabled;
    }

    @GetMapping("/ai-discovery")
    public String aiDiscoveryPage() {
        return localDiscoveryEnabled
                ? "admin/discovery"
                : "admin/discovery-launcher";
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
