package com.warisango.controller;

import com.warisango.model.service.PendingHeritageBusinessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pending-list")
public class PendingHeritageBusinessController {

    private final PendingHeritageBusinessService pendingHeritageBusinessService;

    public PendingHeritageBusinessController(
            PendingHeritageBusinessService pendingHeritageBusinessService) {

        this.pendingHeritageBusinessService = pendingHeritageBusinessService;
    }

    @GetMapping
    public String pendingList(Model model) {
        model.addAttribute("businesses", pendingHeritageBusinessService.findAll());
        return "admin/pending-list";
    }

    @GetMapping("/{businessId}")
    public String pendingDetails(
            @PathVariable String businessId,
            Model model) {

        model.addAttribute(
                "business",
                pendingHeritageBusinessService.findById(businessId)
        );
        return "admin/pending-details";
    }

    @PostMapping("/{businessId}/approve")
    public String approve(
            @PathVariable String businessId,
            RedirectAttributes redirectAttributes) {

        pendingHeritageBusinessService.approve(businessId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Heritage business approved successfully."
        );
        return "redirect:/admin/pending-list";
    }

    @PostMapping("/{businessId}/reject")
    public String reject(
            @PathVariable String businessId,
            RedirectAttributes redirectAttributes) {

        pendingHeritageBusinessService.reject(businessId);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Heritage business rejected successfully."
        );
        return "redirect:/admin/pending-list";
    }
}
