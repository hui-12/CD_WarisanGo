package com.warisango.controller;

import com.warisango.service.ProcessedBusinessService;
import com.warisango.service.AdminBusinessService;
import com.warisango.service.BusinessPhotoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProcessedBusinessController {

    private final ProcessedBusinessService processedBusinessService;
    private final AdminBusinessService adminBusinessService;
    private final BusinessPhotoService businessPhotoService;

    public ProcessedBusinessController(ProcessedBusinessService processedBusinessService,
                                       AdminBusinessService adminBusinessService,
                                       BusinessPhotoService businessPhotoService) {
        this.processedBusinessService = processedBusinessService;
        this.adminBusinessService = adminBusinessService;
        this.businessPhotoService = businessPhotoService;
    }

    @GetMapping("/admin/processed-businesses")
    public String processedBusinesses(Model model) {
        model.addAttribute("entries", processedBusinessService.findAll());
        return "admin/manage-processed-businesses";
    }

    @GetMapping("/admin/audit-log")
    public String legacyAuditLogUrl() {
        return "redirect:/admin/processed-businesses";
    }

    @PostMapping("/admin/processed-businesses/{businessId}/status")
    public String updateStatus(@PathVariable String businessId, @RequestParam boolean active,
                               Authentication authentication, RedirectAttributes attributes) {
        adminBusinessService.setActive(businessId, active, authentication.getName());
        attributes.addFlashAttribute("processedBusinessMessage",
                active ? "Business activated." : "Business deactivated.");
        return "redirect:/admin/processed-businesses";
    }

    @PostMapping("/admin/processed-businesses/{businessId}/photos/{photoId}/remove")
    public String removePhoto(@PathVariable String businessId, @PathVariable String photoId,
                              Authentication authentication, RedirectAttributes attributes) {
        businessPhotoService.takeDownPhoto(photoId, authentication.getName());
        attributes.addFlashAttribute("processedBusinessMessage", "Photo taken down.");
        return "redirect:/admin/processed-businesses";
    }
}
