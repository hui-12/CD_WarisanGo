package com.warisango.controller;

import com.warisango.dto.AdminBusinessView;
import com.warisango.dto.HeritageBusinessUpdateRequest;
import com.warisango.service.AdminBusinessService;
import com.warisango.service.BusinessPhotoService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminBusinessController {
    private final AdminBusinessService businessService;
    private final BusinessPhotoService photoService;

    public AdminBusinessController(AdminBusinessService businessService, BusinessPhotoService photoService) {
        this.businessService = businessService;
        this.photoService = photoService;
    }

    @GetMapping("/admin/businesses/{businessId}/manage")
    public String manage(@PathVariable String businessId,
                         @RequestParam(required = false) String photoId,
                         @RequestParam(required = false) String reportId,
                         Authentication authentication,
                         Model model) {
        AdminBusinessView business = businessService.get(businessId, authentication.getName());
        model.addAttribute("business", business);
        model.addAttribute("businessForm", business.details());
        model.addAttribute("selectedPhotoId", photoId);
        model.addAttribute("sourceReportId", reportId);
        return "admin/business-manage";
    }

    @PostMapping("/admin/businesses/{businessId}/manage")
    public String update(@PathVariable String businessId,
                         @Valid @ModelAttribute("businessForm") HeritageBusinessUpdateRequest request,
                         BindingResult bindingResult,
                         @RequestParam(required = false) String reportId,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("business", businessService.get(businessId, authentication.getName()));
            model.addAttribute("sourceReportId", reportId);
            return "admin/business-manage";
        }
        businessService.update(businessId, request, authentication.getName());
        attributes.addFlashAttribute("adminBusinessMessage", "Business details updated.");
        if (reportId != null && !reportId.isBlank()) {
            return "redirect:/admin/business-reports/" + reportId;
        }
        return "redirect:/admin/businesses/" + businessId + "/manage";
    }

    @PostMapping("/admin/businesses/{businessId}/status")
    public String status(@PathVariable String businessId, @RequestParam boolean active,
                         Authentication authentication, RedirectAttributes attributes) {
        businessService.setActive(businessId, active, authentication.getName());
        attributes.addFlashAttribute("adminBusinessMessage",
                active ? "Business activated." : "Business deactivated.");
        return "redirect:/admin/businesses/" + businessId + "/manage";
    }

    @PostMapping("/admin/businesses/{businessId}/photos/{photoId}/remove")
    public String removePhoto(@PathVariable String businessId, @PathVariable String photoId,
                              Authentication authentication, RedirectAttributes attributes) {
        photoService.takeDownPhoto(photoId, authentication.getName());
        attributes.addFlashAttribute("adminBusinessMessage", "Photo taken down.");
        return "redirect:/admin/businesses/" + businessId + "/manage";
    }
}
