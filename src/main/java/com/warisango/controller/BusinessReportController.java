package com.warisango.controller;

import com.warisango.dto.BusinessReportSummary;
import com.warisango.dto.BusinessReportView;
import com.warisango.service.BusinessReportService;
import com.warisango.service.BusinessPhotoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class BusinessReportController {
    private final BusinessReportService businessReportService;
    private final BusinessPhotoService businessPhotoService;

    public BusinessReportController(BusinessReportService businessReportService,
                                    BusinessPhotoService businessPhotoService) {
        this.businessReportService = businessReportService;
        this.businessPhotoService = businessPhotoService;
    }

    @PostMapping("/business/{businessId}/report")
    public String submit(@PathVariable String businessId,
                         @RequestParam String reason,
                         @RequestParam String details,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            businessReportService.submit(authentication.getName(), businessId, reason, details);
            redirectAttributes.addFlashAttribute("businessReportMessage",
                    "Thank you. Your correction report is pending admin review.");
            return "redirect:/business/" + businessId;
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("businessReportError", exception.getMessage());
            return "redirect:/business/" + businessId + "#report-business";
        }
    }

    @GetMapping("/admin/business-reports")
    public String queue(Authentication authentication, Model model) {
        List<BusinessReportView> reports = businessReportService.getAll(authentication.getName());
        BusinessReportSummary summary = businessReportService.summarize(reports);
        model.addAttribute("reports", reports);
        model.addAttribute("totalReports", summary.total());
        model.addAttribute("pendingReports", summary.pending());
        model.addAttribute("resolvedReports", summary.resolved());
        model.addAttribute("dismissedReports", summary.dismissed());
        var photoReports = businessPhotoService.getReports(authentication.getName());
        model.addAttribute("photoReports", photoReports);
        model.addAttribute("photoReportGroups", businessPhotoService.groupReports(photoReports));
        return "admin/business-report-queue";
    }

    @GetMapping("/admin/business-reports/{reportId}")
    public String detail(@PathVariable String reportId, Authentication authentication, Model model) {
        BusinessReportView report = businessReportService.get(reportId, authentication.getName());
        model.addAttribute("report", report);
        model.addAttribute("businessPhotos", businessPhotoService.getPhotos(report.businessId()));
        return "admin/business-report-detail";
    }

    @PostMapping("/admin/business-reports/{reportId}/resolve-without-changes")
    public String resolveWithoutChanges(@PathVariable String reportId,
                                        @RequestParam(required = false) String resolutionNote,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        return perform(reportId, redirectAttributes, () -> businessReportService.resolveWithoutChanges(
                reportId, authentication.getName(), resolutionNote), "resolved");
    }

    @PostMapping("/admin/business-reports/{reportId}/dismiss")
    public String dismiss(@PathVariable String reportId,
                          @RequestParam(required = false) String resolutionNote,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        return perform(reportId, redirectAttributes, () ->
                businessReportService.dismiss(reportId, authentication.getName(), resolutionNote), "dismissed");
    }

    private String perform(String reportId, RedirectAttributes redirectAttributes,
                           Runnable action, String outcome) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("businessReportAdminMessage", "Report " + outcome + ".");
            return "redirect:/admin/business-reports/" + reportId;
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("businessReportAdminError", exception.getMessage());
            return "redirect:/admin/business-reports/" + reportId;
        }
    }

}
