package com.warisango.controller;

import com.warisango.dto.ReportDTO;
import com.warisango.model.service.ContentModerationService;
import com.warisango.model.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Handles Tourist report submissions and Admin moderation actions.
 */
@Controller
public class ReportController {

    private final ReportService reportService;
    private final ContentModerationService contentModerationService;

    public ReportController(
            ReportService reportService,
            ContentModerationService contentModerationService) {
        this.reportService = reportService;
        this.contentModerationService = contentModerationService;
    }

    @PostMapping("/reviews/reports")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createReport(
            @RequestParam String targetType,
            @RequestParam(required = false) String reviewId,
            @RequestParam(required = false) String commentId,
            @RequestParam String reason,
            Authentication authentication) {

        try {
            reportService.createReport(
                    targetType,
                    reviewId,
                    commentId,
                    reason,
                    authentication.getName()
            );
            return ResponseEntity.ok(Map.of("message", "Thank you. Your report has been submitted."));
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() == null
                    ? "The report could not be submitted."
                    : e.getMessage();
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }
    }

    @PostMapping("/reviews/moderation/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkText(@RequestParam String text) {
        boolean prohibited = contentModerationService.containsProhibitedContent(text);
        return ResponseEntity.ok(Map.of(
                "allowed", !prohibited,
                "message", prohibited ? contentModerationService.getProhibitedMessage() : ""
        ));
    }

    @GetMapping("/admin/reports")
    public String moderationPage(Model model, Authentication authentication) {
        try {
            reportService.requireAdmin(authentication.getName());
            model.addAttribute("reports", reportService.getAllReports());
            model.addAttribute(
                    "currentAdminId",
                    reportService.getCurrentModerationAdminId(authentication.getName())
            );
            return "admin/ReportModerationPage";
        } catch (SecurityException e) {
            return "redirect:/";
        }
    }

    @PostMapping("/admin/reports/{reportId}/dismiss")
    public String dismissReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.dismissReport(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    @PostMapping("/admin/reports/{reportId}/hide")
    public String hideReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.hideReport(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    @PostMapping("/admin/reports/{reportId}/restore")
    public String restoreReport(
            @PathVariable String reportId,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        return resolveAction(
                () -> reportService.restoreReportTarget(reportId, authentication.getName()),
                redirectAttributes
        );
    }

    @PostMapping("/admin/reports/{reportId}/delete")
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

        return "redirect:/admin/reports";
    }
}
