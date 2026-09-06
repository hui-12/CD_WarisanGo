package com.warisango.controller;

import com.warisango.service.BusinessPhotoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BusinessPhotoController {
    private final BusinessPhotoService photoService;

    public BusinessPhotoController(BusinessPhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/business/{businessId}/photos")
    public String upload(@PathVariable String businessId, @RequestParam("photos") MultipartFile[] photos,
                         Authentication authentication, RedirectAttributes attributes) {
        try {
            photoService.upload(businessId, authentication.getName(), photos);
            attributes.addFlashAttribute("businessPhotoMessage", "Your photo was added successfully.");
        } catch (RuntimeException exception) {
            attributes.addFlashAttribute("businessPhotoError", exception.getMessage());
        }
        return "redirect:/business/" + businessId + "#photos";
    }

    @PostMapping("/business/{businessId}/photos/{photoId}/report")
    public String report(@PathVariable String businessId, @PathVariable String photoId,
                         @RequestParam String reason, @RequestParam(required = false) String details,
                         Authentication authentication, RedirectAttributes attributes) {
        try {
            photoService.report(photoId, authentication.getName(), reason, details);
            attributes.addFlashAttribute("businessPhotoReportMessage",
                    "Thank you for reporting this photo. Our admin team will review it.");
        } catch (RuntimeException exception) {
            attributes.addFlashAttribute("businessPhotoError", exception.getMessage());
        }
        return "redirect:/business/" + businessId + "#photos";
    }

    @GetMapping("/admin/photo-reports")
    public String legacyPhotoReports() {
        return "redirect:/admin/business-reports#photo-reports";
    }

    @PostMapping("/admin/business-reports/photos/{reportId}/dismiss")
    public String dismiss(@PathVariable String reportId, Authentication authentication,
                          RedirectAttributes attributes) {
        return moderate(attributes, () -> photoService.dismiss(reportId, authentication.getName()),
                "Report dismissed.");
    }

    @PostMapping("/admin/business-reports/photos/{reportId}/remove")
    public String remove(@PathVariable String reportId, Authentication authentication,
                         RedirectAttributes attributes) {
        return moderate(attributes, () -> photoService.takeDown(reportId, authentication.getName()),
                "Photo removed.");
    }

    private String moderate(RedirectAttributes attributes, Runnable action, String message) {
        try {
            action.run();
            attributes.addFlashAttribute("photoReportMessage", message);
        } catch (RuntimeException exception) {
            attributes.addFlashAttribute("photoReportError", exception.getMessage());
        }
        return "redirect:/admin/business-reports#photo-reports";
    }
}
