package com.warisango.controller;

import com.warisango.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles Admin review and comment moderation pages.
 */
@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/admin/review-reports")
    public String moderationPage(Model model, Authentication authentication) {
        try {
            reportService.requireAdmin(authentication.getName());
            model.addAttribute("reports", reportService.getAllReports());
            model.addAttribute(
                    "currentAdminId",
                    reportService.getCurrentModerationAdminId(authentication.getName())
            );
            return "admin/report-moderation";
        } catch (SecurityException e) {
            return "redirect:/";
        }
    }

    @PostMapping("/admin/review-reports/{reportId}/dismiss")
    public String dismissReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.dismissReport(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    @PostMapping("/admin/review-reports/{reportId}/hide")
    public String hideReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.hideReport(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    @PostMapping("/admin/review-reports/{reportId}/restore")
    public String restoreReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.restoreReportTarget(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    @PostMapping("/admin/review-reports/{reportId}/delete")
    public String deleteReportTarget(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.deleteReportTarget(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    private String resolveAction(Runnable action, RedirectAttributes redirectAttributes) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("moderationMessage", "Moderation action completed.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("moderationError", "Admin access is required.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("moderationError", e.getMessage());
        }

        return "redirect:/admin/review-reports";
    }
}
